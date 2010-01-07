/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2010, Benedikt Huber (benedikt.huber@gmail.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
/* Autogenerated using ERB (tiny ruby, part of ruby distributions) */
/* erb LoadOnReturn.erb > LoadOnReturn.java */

package wcet.devel;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class ObjectCache {
    /**
	 * Set to false for the WCET analysis, true for measurement
	 */
    final static boolean MEASURE = false;
	static int ts, te, to;
    static int cs, ce;
    /* classes */
    public abstract static class Obj1 {
        /* self references */
        public Obj1 next;
        public Obj1[] preds;
        /* down references */
        public Obj2 sub1;
        public Obj3 sub2;
        public Obj3[] subs;
        /* primitive array and fields */
        public int val;
        public int[] vals;
        public long lval;
        public long[] lvals;
        public static Obj1 create() {
            if(to > 10) return new Obj1a();
            else        return new Obj1b();
        }
	    public int test2(Obj1 other, Obj2 sub) {
	        int v1 = this.next.val;  // GF: $this, $this.next
	        int v2 = other.next.val; // GF: $arg0, $arg0.next
	        Obj2 ssub = this.selectSub2(other,sub); // ssub = {$0.sub1,$0.next.next.sub1,  // Obj1a
	                                                //         $1.sub1, $1.next.next.sub1, // Obj1b
	                                                //         $2 }                        // both
	        int v3 = ssub.val;       // GF: ssub.val
	        return v1+v2+v3;
	    }
	    protected abstract Obj2 selectSub2(Obj1 other, Obj2 def);
    }
    public static class Obj1a extends Obj1 {
        protected Obj2 selectSub2(Obj1 other, Obj2 def) {
            Obj2 selected;
            if(te > 5) selected = this.sub1;
            else if(te > 7) selected = this.next.next.sub1;
            else             selected = def;
            return selected;
        }
    }
    public static class Obj1b extends Obj1 {
        protected Obj2 selectSub2(Obj1 other, Obj2 def) {
            Obj2 selected;
            if(te > 10)      selected = other.sub1;
            else if(te > 12) selected = other.next.next.sub1;
            else             selected = def;
            return selected;
        }
    }
    public static class Obj2 {
        /* primitive array and fields */
        public int val;
        public int[] vals;
        public long lval;
        public long[] lvals;        
    }
    public static class Obj3 {
        /* primitive array and fields */
        public int val;
        public int[] vals;
        public long lval;
        public long[] lvals;        
    }
    /* static references */
    static Obj1 obj1a, obj1b;
    static Obj2 obj2a, obj2b;
    static Obj3 obj3a, obj3b;
    /* static arrays */
    static Obj2[] obj2s;
    static Obj3[] obj3s;
    public static void init()
    {
        obj1a = Obj1.create(); obj1b = Obj1.create();
        obj2a = new Obj2(); obj2b = new Obj2();
        obj3a = new Obj3(); obj3b = new Obj3();
        obj1a.sub1 = obj2a; obj1b.sub1 = obj2a;
        obj1a.sub2 = obj3a; obj1b.sub2 = obj3b;     
    }
	public static void main(String[] args) {

		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		init();
		int v = invoke();
		System.out.print("Value: ");
		System.out.print(v);
		int val = te-ts-to;
		int cacheCost = ce-cs;
	}
	
	static int invoke() {
		if (MEASURE) te = Native.rdMem(Const.IO_CNT);
		return measure();
	}
	
	static int measure() {
		if (MEASURE) ts = Native.rdMem(Const.IO_CNT);
		int val = 0;
		val += test1();
		val += test2();
		val += test3();
		return val;
	}
	/* test1: access field of one out of two static objects */
	static int test1() {
	    Obj1 obj1;
	    if(to > 10) {
	        obj1 = obj1a;
	    } else {
	        obj1 = obj1b;
	    }
	    return obj1.sub1.val + obj1.sub2.val;
	}

	static int test2() {
	    return obj1a.test2(obj1b,obj2a);
	}

	// here the number of objects accesses is unbounded [assuming unknown loop
	// bounds] in the second part
	static int test3() {
        Obj1 obj1;
        if(to > 10) {
            obj1 = obj1a;
        } else {
            obj1 = obj1b;
        }
        int v = obj1.val;            // {obj1a,ojb1b}.val
        Obj1 obj1a = obj1.next;      // {obj1a,obj1b}.next
	    for(int i = 0; i < 100; i++) { //@WCA loop = 100
	        if(obj1.sub1.val > 0) v += obj1.sub1.val;  // top * 4
	        else                  v *= obj1a.sub1.val; // obj1a, obj1a.sub1
	        obj1 = obj1.next;                          // top
	    }
	    return v;
	}
	// Enhanced bug test case from wolfgang (5.1.2010)
	static int test4() {
	    int v = obj1a.next.val;         // GF: obj1a, obj1a.next
		if(v == to) obj1a.next = obj1b; // heap modification -> TOP		
		return v+obj1a.next.val;        // GF: top, top
	}
	
}
