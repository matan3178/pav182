public class TestClass {
	public int avExample(int x, int y, int a) {
		int w = 9;
		int z = x + y;
		w = z;
		int b = z;
		b = avExample(b, b, b);
		
		while (a == b) {
			a = a - 1;
			int c = x + y + w;
			b = c + a;
		}
		return b;
	}
}