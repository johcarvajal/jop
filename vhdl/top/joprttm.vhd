--
--
--  This file is a part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)
--
--  This program is free software: you can redistribute it and/or modify
--  it under the terms of the GNU General Public License as published by
--  the Free Software Foundation, either version 3 of the License, or
--  (at your option) any later version.
--
--  This program is distributed in the hope that it will be useful,
--  but WITHOUT ANY WARRANTY; without even the implied warranty of
--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--  GNU General Public License for more details.
--
--  You should have received a copy of the GNU General Public License
--  along with this program.  If not, see <http://www.gnu.org/licenses/>.
--


--
--	joprttm.vhd
--
--	top level for transactional memory multiprocessor, cycore board with EP1C12
--
--	2009-07-19	copied from jopmul.vhd


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use ieee.math_real.log2;
use ieee.math_real.ceil;

use work.jop_types.all;
use work.sc_pack.all;
use work.sc_arbiter_pack.all;
use work.jop_config.all;

entity jop is

generic (
	ram_cnt		: integer := 2;		-- clock cycles for external ram
--	rom_cnt		: integer := 3;		-- clock cycles for external rom OK for 20 MHz
	rom_cnt		: integer := 10;	-- clock cycles for external rom for 100 MHz
	jpc_width	: integer := 10;	-- address bits of java bytecode pc = cache size
	block_bits	: integer := 4;		-- 2*block_bits is number of cache blocks
	spm_width	: integer := 8;		-- size of scratchpad RAM (in number of address bits for 32-bit words)
	cpu_cnt		: integer := 2		-- number of cpus
);

port (
	clk		: in std_logic;
--
--	serial interface
--
	ser_txd			: out std_logic;
	ser_rxd			: in std_logic;
	ser_ncts		: in std_logic;
	ser_nrts		: out std_logic;

--
--	watchdog
--
	wd		: out std_logic;
	freeio	: out std_logic;

--
--	two ram banks
--
	rama_a		: out std_logic_vector(17 downto 0);
	rama_d		: inout std_logic_vector(15 downto 0);
	rama_ncs	: out std_logic;
	rama_noe	: out std_logic;
	rama_nlb	: out std_logic;
	rama_nub	: out std_logic;
	rama_nwe	: out std_logic;
	ramb_a		: out std_logic_vector(17 downto 0);
	ramb_d		: inout std_logic_vector(15 downto 0);
	ramb_ncs	: out std_logic;
	ramb_noe	: out std_logic;
	ramb_nlb	: out std_logic;
	ramb_nub	: out std_logic;
	ramb_nwe	: out std_logic;

--
--	config/program flash and big nand flash
--
	fl_a	: out std_logic_vector(18 downto 0);
	fl_d	: inout std_logic_vector(7 downto 0);
	fl_ncs	: out std_logic;
	fl_ncsb	: out std_logic;
	fl_noe	: out std_logic;
	fl_nwe	: out std_logic;
	fl_rdy	: in std_logic;

--
--	I/O pins of board
--
--	io_b	: inout std_logic_vector(10 downto 1);
	io_l	: inout std_logic_vector(20 downto 1);
	io_r	: inout std_logic_vector(20 downto 1);
	io_t	: inout std_logic_vector(6 downto 1)
	
);
end jop;

architecture rtl of jop is

--
--	constants:
--

constant tm_addr_width		: integer := 18;	-- address bits of cachable memory
constant tm_way_bits		: integer := 5;		-- 2**way_bits is number of entries


--
--	components:
--

component pll is
generic (multiply_by : natural; divide_by : natural);
port (
	inclk0		: in std_logic;
	c0			: out std_logic
);
end component;

--
--	Signals
--
	signal clk_int			: std_logic;

	signal int_res			: std_logic;
	signal res_cnt			: unsigned(2 downto 0) := "000";	-- for the simulation

	attribute altera_attribute : string;
	attribute altera_attribute of res_cnt : signal is "POWER_UP_LEVEL=LOW";

--
--	jopcpu connections
--
	signal sc_out_tm		: arb_out_type(0 to cpu_cnt-1);
	signal sc_in_tm			: arb_in_type(0 to cpu_cnt-1);
	
	signal sc_out_arb		: arb_out_type(0 to cpu_cnt-1);
	signal sc_in_arb		: arb_in_type(0 to cpu_cnt-1);
		
	signal sc_mem_out	: sc_out_type;
	signal sc_mem_in	: sc_in_type;
	
	
	signal sc_io_out		: sc_out_array_type(0 to cpu_cnt-1);
	signal sc_io_in			: sc_in_array_type(0 to cpu_cnt-1);
	signal irq_in			  : irq_in_array_type(0 to cpu_cnt-1);
	signal irq_out			: irq_out_array_type(0 to cpu_cnt-1);
	signal exc_req			: exception_array_type(0 to cpu_cnt-1);

--
--	IO interface
--
-- 	signal ser_in			: ser_in_type;
-- 	signal ser_out			: ser_out_type;
	type wd_out_array is array (0 to cpu_cnt-1) of std_logic;
	signal wd_out			: wd_out_array;

	-- for generation of internal reset

-- memory interface

	signal ram_addr			: std_logic_vector(17 downto 0);
	signal ram_dout			: std_logic_vector(31 downto 0);
	signal ram_din			: std_logic_vector(31 downto 0);
	signal ram_dout_en	: std_logic;
	signal ram_ncs			: std_logic;
	signal ram_noe			: std_logic;
	signal ram_nwe			: std_logic;

-- cmpsync

	signal sync_in_array	: sync_in_array_type(0 to cpu_cnt-1);
	signal sync_out_array	: sync_out_array_type(0 to cpu_cnt-1);
	
-- remove the comment for RAM access counting
-- signal ram_count		: std_logic;

--
--	TM
--
	
	signal exc_tm_rollback	: std_logic_vector(0 to cpu_cnt-1);
	signal tm_broadcast		: tm_broadcast_type;
	signal tm_broadcast_del	: tm_broadcast_type;
	
	signal commit_try		: std_logic_vector(0 to cpu_cnt-1);
	signal commit_allow		: std_logic_vector(0 to cpu_cnt-1);
	
	
	
begin

--
--	intern reset
--	no extern reset, epm7064 has too less pins
--

process(clk_int)
begin
	if rising_edge(clk_int) then
		if (res_cnt/="111") then
			res_cnt <= res_cnt+1;
		end if;

		int_res <= not res_cnt(0) or not res_cnt(1) or not res_cnt(2);
	end if;
end process;

--
--	components of jop
--
	pll_inst : pll generic map(
		multiply_by => pll_mult,
		divide_by => pll_div
	)
	port map (
		inclk0	 => clk,
		c0	 => clk_int
	);
-- clk_int <= clk;
	
-- process(wd_out)
-- variable wd_help : std_logic;
-- 	begin
-- 		wd_help := '0';
-- 		for i in 0 to cpu_cnt-1 loop
-- 			wd_help := wd_help or wd_out(i);
-- 		end loop;
-- 		wd <= wd_help;
-- end process;

	wd <= wd_out(0);
	
	gen_cpu: for i in 0 to cpu_cnt-1 generate
		cmp_cpu: entity work.jopcpu
			generic map(
				jpc_width => jpc_width,
				block_bits => block_bits,
				spm_width => spm_width
			)
			port map(clk_int, int_res,
				sc_out_tm(i), sc_in_tm(i),
				sc_io_out(i), sc_io_in(i), irq_in(i), 
				irq_out(i), exc_req(i), exc_tm_rollback(i));
	end generate;
	
	gen_tm: for i in 0 to cpu_cnt-1 generate
		cmp_tm: entity work.tmif
			generic map (
				addr_width => tm_addr_width,
				way_bits => tm_way_bits
			)	
			port map (
				clk	=> clk_int,
				reset => int_res,
				
				commit_out_try => commit_try(i),
				commit_in_allow => commit_allow(i),
			
				broadcast => tm_broadcast_del,
			
				sc_out_cpu => sc_out_tm(i),  
				sc_in_cpu => sc_in_tm(i), 
			
				sc_out_arb => sc_out_arb(i),
				sc_in_arb => sc_in_arb(i),
			
				exc_tm_rollback => exc_tm_rollback(i)
				);
	end generate;

	cmp_coordinator: entity work.tm_coordinator(rtl)
	generic map (
		cpu_cnt => cpu_cnt
		)
	port map (
		clk => clk_int,
		reset => int_res,
		commit_try => commit_try,
		commit_allow => commit_allow
		);
			
	cmp_arbiter: entity work.arbiter
		generic map(
			addr_bits => SC_ADDR_SIZE,
			cpu_cnt => cpu_cnt
		)
		port map(clk_int, int_res,
			sc_out_arb, sc_in_arb,
			sc_mem_out, sc_mem_in,
			tm_broadcast
			-- Enable for use with Round Robin Arbiter
			-- sync_out_array(1)
			);
			
	del_tm_broadcast: process (clk_int, int_res) is
	begin
	    if int_res = '1' then
	    	tm_broadcast_del <= ('0', (others => '0')); 
	    elsif rising_edge(clk_int) then
	    	tm_broadcast_del.valid <= tm_broadcast.valid;
		 	if tm_broadcast.valid = '1' then
				tm_broadcast_del.address <= tm_broadcast.address;
			end if;
	    end if;
	end process del_tm_broadcast;


	cmp_scm: entity work.sc_mem_if
		generic map (
			ram_ws => ram_cnt-1,
			rom_ws => rom_cnt-1
		)
		port map (clk_int, int_res,
			sc_mem_out, sc_mem_in,

			ram_addr => ram_addr,
			ram_dout => ram_dout,
			ram_din => ram_din,
			ram_dout_en	=> ram_dout_en,
			ram_ncs => ram_ncs,
			ram_noe => ram_noe,
			ram_nwe => ram_nwe,

			fl_a => fl_a,
			fl_d => fl_d,
			fl_ncs => fl_ncs,
			fl_ncsb => fl_ncsb,
			fl_noe => fl_noe,
			fl_nwe => fl_nwe,
			fl_rdy => fl_rdy

		);
		
	-- syncronization of processors
	cmp_sync: entity work.cmpsync generic map (
		cpu_cnt => cpu_cnt)
		port map
		(
			clk => clk_int,
			reset => int_res,
			sync_in_array => sync_in_array,
			sync_out_array => sync_out_array
		);
	
	-- io for processor 0
	cmp_io: entity work.scio generic map (
			cpu_id => 0,
			cpu_cnt => cpu_cnt,
			auto_disable_hw_exceptions => true
		)
		port map (clk_int, int_res,
			sc_io_out(0), sc_io_in(0),
			irq_in(0), irq_out(0), exc_req(0),

			sync_out => sync_out_array(0),
			sync_in => sync_in_array(0),

			txd => ser_txd,
			rxd => ser_rxd,
			ncts => ser_ncts,
			nrts => ser_nrts,
			wd => wd_out(0),
			l => io_l,
			r => io_r,
			t => io_t
			--b => io_b
			-- remove the comment for RAM access counting
			-- ram_cnt => ram_count			
		);
	
	-- io for processors with only sc_sys
	gen_io: for i in 1 to cpu_cnt-1 generate
		cmp_io2: entity work.sc_sys generic map (
			addr_bits => 4,
			clk_freq => clk_freq,
			cpu_id => i,
			cpu_cnt => cpu_cnt,
			auto_disable_hw_exceptions => true
		)
		port map(
			clk => clk_int,
			reset => int_res,
			address => sc_io_out(i).address(3 downto 0),
			wr_data => sc_io_out(i).wr_data,
			rd => sc_io_out(i).rd,
			wr => sc_io_out(i).wr,
			rd_data => sc_io_in(i).rd_data,
			rdy_cnt => sc_io_in(i).rdy_cnt,
			
			irq_in => irq_in(i),
			irq_out => irq_out(i),
			exc_req => exc_req(i),
			
			sync_out => sync_out_array(i),
			sync_in => sync_in_array(i),
			wd => wd_out(i)
			-- remove the comment for RAM access counting
			-- ram_count => ram_count
		);

	end generate;
	

	process(ram_dout_en, ram_dout)
	begin
		if ram_dout_en='1' then
			rama_d <= ram_dout(15 downto 0);
			ramb_d <= ram_dout(31 downto 16);
		else
			rama_d <= (others => 'Z');
			ramb_d <= (others => 'Z');
		end if;
	end process;

	ram_din <= ramb_d & rama_d;
	
	-- remove the comment for RAM access counting
	-- ram_count <= ram_ncs;

--
--	To put this RAM address in an output register
--	we have to make an assignment (FAST_OUTPUT_REGISTER)
--
	rama_a <= ram_addr;
	rama_ncs <= ram_ncs;
	rama_noe <= ram_noe;
	rama_nwe <= ram_nwe;
	rama_nlb <= '0';
	rama_nub <= '0';

	ramb_a <= ram_addr;
	ramb_ncs <= ram_ncs;
	ramb_noe <= ram_noe;
	ramb_nwe <= ram_nwe;
	ramb_nlb <= '0';
	ramb_nub <= '0';

	freeio <= 'Z';

end rtl;
