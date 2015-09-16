package cx;

import java.io.File;
import java.util.List;
import junit.framework.TestCase;
import cx.handlers.StringHandler;
import cx.runtime.BreakPoint;
import cx.runtime.ContextFrame;

public class TestMD5 extends TestCase {
	@SuppressWarnings("rawtypes")
	public void testMD5() {
		BreakPoint myBreakPoint = new BreakPoint() {
			public void run(int line, ContextFrame cx) {
				if (line == 143) {
					System.out.println("tail=" + cx.get("tail"));

					long[] arr = new long[] { 1819043176, 32879, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0 };
					List list = (List) cx.get("tail");
					for (int i = 0; i < list.size(); i++) {
						if (list.get(i) != null) {
							assertEquals(arr[i], ((Number) list.get(i)).longValue());
						}
					}
					System.out.println("state=" + cx.get("state"));
					arr = new long[] { 1732584193, -271733879, -1732584194, 271733878 };
					list = (List) cx.get("state");
					for (int i = 0; i < list.size(); i++) {
						if (list.get(i) != null) {
							assertEquals(arr[i] & 0x0FFFFFFFFL, ((Number) list.get(i)).longValue() & 0x0FFFFFFFFL);
						}
					}

				} else if (line == 144) {
					System.out.println("state=" + cx.get("state"));

					long[] arr = new long[] { 708854109, 1982483388, 2443014585L, 2462390032L };
					List list = (List) cx.get("state");
					for (int i = 0; i < list.size(); i++) {
						if (list.get(i) != null) {
							assertEquals(arr[i] & 0x0FFFFFFFFL, ((Number) list.get(i)).longValue() & 0x0FFFFFFFFL);
						}
					}
				}
			}
		};

		Context cx = new Context();
		cx.addHandler(new StringHandler());
		cx.setBreakpoints(new int[] { 143, 144 }, myBreakPoint);
		cx.evaluate((new Parser(new File("md5.cx"))).parse());

		cx.evaluate((new Parser("digest = cmn(1,2,3,4,5,6);")).parse());
		assertEquals(cmn(1, 2, 3, 4, 5, 6), ((Number) cx.get("digest")).longValue());
		cx.evaluate((new Parser("digest = cmn(-1,-2,-3,-4,-5,-6);")).parse());
		assertEquals(cmn(-1, -2, -3, -4, -5, -6), ((Number) cx.get("digest")).longValue());

		cx.evaluate((new Parser("digest = ff(1,2,3,4,5,6,7);")).parse());
		assertEquals(ff(1, 2, 3, 4, 5, 6, 7), ((Number) cx.get("digest")).longValue());
		cx.evaluate((new Parser("digest = ff(-1,-2,-3,-4,-5,-6,-7);")).parse());
		assertEquals(ff(-1, -2, -3, -4, -5, -6, -7), ((Number) cx.get("digest")).longValue());

		cx.evaluate((new Parser("digest = gg(1,2,3,4,5,6,7);")).parse());
		assertEquals(gg(1, 2, 3, 4, 5, 6, 7), ((Number) cx.get("digest")).longValue());
		cx.evaluate((new Parser("digest = gg(-1,-2,-3,-4,-5,-6,-7);")).parse());
		assertEquals(gg(-1, -2, -3, -4, -5, -6, -7), ((Number) cx.get("digest")).longValue());

		cx.evaluate((new Parser("digest = hh(1,2,3,4,5,6,7);")).parse());
		assertEquals(hh(1, 2, 3, 4, 5, 6, 7), ((Number) cx.get("digest")).longValue());
		cx.evaluate((new Parser("digest = hh(-1,-2,-3,-4,-5,-6,-7);")).parse());
		assertEquals(hh(-1, -2, -3, -4, -5, -6, -7), ((Number) cx.get("digest")).longValue());

		cx.evaluate((new Parser("digest = ii(1,2,3,4,5,6,7);")).parse());
		assertEquals(ii(1, 2, 3, 4, 5, 6, 7), ((Number) cx.get("digest")).longValue());
		cx.evaluate((new Parser("digest = ii(-1,-2,-3,-4,-5,-6,-7);")).parse());
		assertEquals(ii(-1, -2, -3, -4, -5, -6, -7), ((Number) cx.get("digest")).longValue());

		cx.evaluate((new Parser(
				"arr1 = [-1,-2,-3,-4];arr2 = [-1,-2,-3,-4,-5,-6,-7,-8,-9,-10,-11,-12,-13,-14,-15,-16];md5cycle(arr1,arr2);"))
				.parse());
		long[] arr1 = new long[] { -1, -2, -3, -4 };
		long[] arr2 = new long[] { -1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -14, -15, -16 };
		md5cycle(arr1, arr2);
		List list = (List) cx.get("arr1");
		for (int i = 0; i < arr1.length; i++) {
			assertEquals(arr1[i], ((Number) list.get(i)).longValue());
		}
		long[] arr1correct = new long[] { -438995042, -2102376289, -231602493, -1253394063 };
		for (int i = 0; i < arr1.length; i++) {
			assertEquals(arr1[i] & 0x0FFFFFFFFL, arr1correct[i] & 0x0FFFFFFFFL);
		}

		// state =1732584193,-271733879,-1732584194,271733878
		// tail =1819043176,32879,0,0,0,0,0,0,0,0,0,0,0,0,40,0
		cx.evaluate((new Parser(
				"arr1 = [1732584193,-271733879,-1732584194,271733878];arr2 = [1819043176,32879,0,0,0,0,0,0,0,0,0,0,0,0,40,0];md5cycle(arr1,arr2);"))
				.parse());
		arr1 = new long[] { 1732584193, -271733879, -1732584194, 271733878 };
		arr2 = new long[] { 1819043176, 32879, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 40, 0 };
		md5cycle(arr1, arr2);
		list = (List) cx.get("arr1");
		for (int i = 0; i < arr1.length; i++) {
			assertEquals(arr1[i], ((Number) list.get(i)).longValue());
		}
		arr1correct = new long[] { 708854109, 1982483388, -1851952711, -1832577264 };
		for (int i = 0; i < arr1.length; i++) {
			assertEquals(arr1[i] & 0x0FFFFFFFFL, arr1correct[i] & 0x0FFFFFFFFL);
		}

		cx.evaluate((new Parser("arr = md5blk('hello');")).parse());
		long[] arr = md5blk("hello");
		list = (List) cx.get("arr");
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) != null) {
				assertEquals(arr[i], ((Number) list.get(i)).longValue());
			}
		}
		long[] check = new long[] { 708854109, 1982483388, 2443014585L, 2462390032L };
		arr = md51("hello");
		for (int i = 0; i < arr.length; i++) {
			assertEquals(arr[i] & 0x0FFFFFFFFL, check[i] & 0x0FFFFFFFFL);
		}

		cx.evaluate((new Parser("arr = md51('hello');")).parse());
		list = (List) cx.get("arr");
		for (int i = 0; i < arr.length; i++) {
			if (list.get(i) != null) {
				assertEquals(arr[i] & 0x0FFFFFFFFL, ((Number) list.get(i)).longValue() & 0x0FFFFFFFFL);
			}
		}

		cx.evaluate((new Parser("digest = md5('hello');")).parse());
		assertEquals("5d41402abc4b2a76b9719d911017c592", cx.get("digest"));
	}

	public void testMoreMD5() {
		Context cx = new Context();
		cx.addHandler(new StringHandler());
		cx.evaluate((new Parser(new File("md5.cx"))).parse());
		cx.evaluate((new Parser("digest = md5('0123456789012345678901234567890123456789012345678901234567');")).parse());
		assertEquals("66f6bb54a54f967caa2607ad2990ecb4", cx.get("digest"));

		cx.evaluate((new Parser(
				"digest = md5('01234567890123456789012345678901234567890123456789012345678901234567890123456789');"))
				.parse());
		assertEquals("0faef1f4cb01d560d59016a2d5e91da6", cx.get("digest"));
	}

	long add32(long a, long b) {
		return ((a & 0x0FFFFFFFFL) + (b & 0x0FFFFFFFFL)) & 0x0FFFFFFFFL;
	}

	long cmn(long q, long a, long b, long x, long s, long t) {
		a = add32(add32(a, q), add32(x, t));
		return add32((a << s) | (a >>> (32 - s)), b);
	}

	long ff(long a, long b, long c, long d, long x, long s, long t) {
		return cmn((b & c) | ((~b) & d), a, b, x, s, t);
	}

	long gg(long a, long b, long c, long d, long x, long s, long t) {
		return cmn((b & d) | (c & (~d)), a, b, x, s, t);
	}

	long hh(long a, long b, long c, long d, long x, long s, long t) {
		return cmn(b ^ c ^ d, a, b, x, s, t);
	}

	long ii(long a, long b, long c, long d, long x, long s, long t) {
		return cmn(c ^ (b | (~d)), a, b, x, s, t);
	}

	void md5cycle(long[] x, long[] k) {
		long a = x[0], b = x[1], c = x[2], d = x[3];

		a = ff(a, b, c, d, k[0], 7, -680876936);
		d = ff(d, a, b, c, k[1], 12, -389564586);
		c = ff(c, d, a, b, k[2], 17, 606105819);
		b = ff(b, c, d, a, k[3], 22, -1044525330);
		a = ff(a, b, c, d, k[4], 7, -176418897);
		d = ff(d, a, b, c, k[5], 12, 1200080426);
		c = ff(c, d, a, b, k[6], 17, -1473231341);
		b = ff(b, c, d, a, k[7], 22, -45705983);
		a = ff(a, b, c, d, k[8], 7, 1770035416);
		d = ff(d, a, b, c, k[9], 12, -1958414417);
		c = ff(c, d, a, b, k[10], 17, -42063);
		b = ff(b, c, d, a, k[11], 22, -1990404162);
		a = ff(a, b, c, d, k[12], 7, 1804603682);
		d = ff(d, a, b, c, k[13], 12, -40341101);
		c = ff(c, d, a, b, k[14], 17, -1502002290);
		b = ff(b, c, d, a, k[15], 22, 1236535329);

		a = gg(a, b, c, d, k[1], 5, -165796510);
		d = gg(d, a, b, c, k[6], 9, -1069501632);
		c = gg(c, d, a, b, k[11], 14, 643717713);
		b = gg(b, c, d, a, k[0], 20, -373897302);
		a = gg(a, b, c, d, k[5], 5, -701558691);
		d = gg(d, a, b, c, k[10], 9, 38016083);
		c = gg(c, d, a, b, k[15], 14, -660478335);
		b = gg(b, c, d, a, k[4], 20, -405537848);
		a = gg(a, b, c, d, k[9], 5, 568446438);
		d = gg(d, a, b, c, k[14], 9, -1019803690);
		c = gg(c, d, a, b, k[3], 14, -187363961);
		b = gg(b, c, d, a, k[8], 20, 1163531501);
		a = gg(a, b, c, d, k[13], 5, -1444681467);
		d = gg(d, a, b, c, k[2], 9, -51403784);
		c = gg(c, d, a, b, k[7], 14, 1735328473);
		b = gg(b, c, d, a, k[12], 20, -1926607734);

		a = hh(a, b, c, d, k[5], 4, -378558);
		d = hh(d, a, b, c, k[8], 11, -2022574463);
		c = hh(c, d, a, b, k[11], 16, 1839030562);
		b = hh(b, c, d, a, k[14], 23, -35309556);
		a = hh(a, b, c, d, k[1], 4, -1530992060);
		d = hh(d, a, b, c, k[4], 11, 1272893353);
		c = hh(c, d, a, b, k[7], 16, -155497632);
		b = hh(b, c, d, a, k[10], 23, -1094730640);
		a = hh(a, b, c, d, k[13], 4, 681279174);
		d = hh(d, a, b, c, k[0], 11, -358537222);
		c = hh(c, d, a, b, k[3], 16, -722521979);
		b = hh(b, c, d, a, k[6], 23, 76029189);
		a = hh(a, b, c, d, k[9], 4, -640364487);
		d = hh(d, a, b, c, k[12], 11, -421815835);
		c = hh(c, d, a, b, k[15], 16, 530742520);
		b = hh(b, c, d, a, k[2], 23, -995338651);

		a = ii(a, b, c, d, k[0], 6, -198630844);
		d = ii(d, a, b, c, k[7], 10, 1126891415);
		c = ii(c, d, a, b, k[14], 15, -1416354905);
		b = ii(b, c, d, a, k[5], 21, -57434055);
		a = ii(a, b, c, d, k[12], 6, 1700485571);
		d = ii(d, a, b, c, k[3], 10, -1894986606);
		c = ii(c, d, a, b, k[10], 15, -1051523);
		b = ii(b, c, d, a, k[1], 21, -2054922799);
		a = ii(a, b, c, d, k[8], 6, 1873313359);
		d = ii(d, a, b, c, k[15], 10, -30611744);
		c = ii(c, d, a, b, k[6], 15, -1560198380);
		b = ii(b, c, d, a, k[13], 21, 1309151649);
		a = ii(a, b, c, d, k[4], 6, -145523070);
		d = ii(d, a, b, c, k[11], 10, -1120210379);
		c = ii(c, d, a, b, k[2], 15, 718787259);
		b = ii(b, c, d, a, k[9], 21, -343485551);

		x[0] = add32(a, x[0]);
		x[1] = add32(b, x[1]);
		x[2] = add32(c, x[2]);
		x[3] = add32(d, x[3]);
	}

	long[] md5blk(String str) {
		char[] s = new char[64];
		str.getChars(0, str.length(), s, 0);
		long[] md5blks = new long[16];
		for (int i = 0; i < 64; i += 4) {
			md5blks[i >> 2] = s[i] + (s[i + 1] << 8) + (s[i + 2] << 16) + (s[i + 3] << 24);
		}
		return md5blks;
	}

	long[] md51(String s) {
		int n = s.length();
		long[] state = new long[] { 1732584193, -271733879, -1732584194, 271733878 };
		int i;
		for (i = 64; i <= n; i += 64) {
			md5cycle(state, md5blk(s.substring(i - 64, i)));
		}
		s = s.substring(i - 64);

		long[] tail = new long[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		for (i = 0; i < s.length(); i++) {
			tail[i >> 2] |= (s.charAt(i) << ((i % 4) << 3));
		}
		tail[i >> 2] |= (0x80 << ((i % 4) << 3));
		if (i > 55) {
			md5cycle(state, tail);
			for (i = 0; i < 16; i++) {
				tail[i] = 0;
			}
		}
		tail[14] = (n * 8);
		System.out.println("j state=" + arrayToString(state));
		System.out.println("j tail=" + arrayToString(tail));
		md5cycle(state, tail);
		System.out.println("j state=" + arrayToString(state));
		return state;
	}

	private String arrayToString(long[] tail) {
		StringBuilder buf = new StringBuilder();
		for (long l : tail) {
			buf.append(l).append(',');
		}
		return buf.toString();
	}

	public void testMD5class() {
		Context cx = new Context();
		cx.addHandler(new StringHandler());
		cx.evaluate((new Parser(new File("md5class.cx"))).parse());

		cx.evaluate((new Parser("digest = MD5.digest('hello');")).parse());
		assertEquals("5d41402abc4b2a76b9719d911017c592", cx.get("digest"));

		cx.evaluate((new Parser("digest = MD5.digest('0123456789012345678901234567890123456789012345678901234567');"))
				.parse());
		assertEquals("66f6bb54a54f967caa2607ad2990ecb4", cx.get("digest"));

		cx.evaluate((new Parser(
				"digest = MD5.digest('01234567890123456789012345678901234567890123456789012345678901234567890123456789');"))
				.parse());
		assertEquals("0faef1f4cb01d560d59016a2d5e91da6", cx.get("digest"));
	}
}
