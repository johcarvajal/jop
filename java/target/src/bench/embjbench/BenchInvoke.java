package embjbench;

class BenchInvoke extends BenchMark {

/*
   0:	iload_1
   1:	iload_2
   2:	iadd
   3:	ireturn
*/
	int add(int a, int b) {

		return a+b;
	}
		

/*
   9:	iload_2
   10:	aload_0
   11:	iload_3
   12:	iload_3
   13:	invokevirtual	#2; //Method add:(II)I
   16:	iadd
   17:	istore_2
*/
	int test(int cnt) {

		int a = 0;
		int i;

		for (i=0; i<cnt; ++i) {
			a += add(i, i);
		}
		return a;
	}

/*
   9:	iload_2
   10:	iload_3
   11:	iload_3
   12:	iadd
   13:	iadd
   14:	istore_2
*/
	int overhead(int cnt) {

		int a = 0;
		int i;

		for (i=0; i<cnt; ++i) {
			a += i+i;
		}
		return a;
	}


	String getName() {

		return "invoke";
	}

	public static void main(String[] args) {

		BenchMark bm = new BenchInvoke();

		Execute.perform(bm);
for (;;);
	}
			
}
