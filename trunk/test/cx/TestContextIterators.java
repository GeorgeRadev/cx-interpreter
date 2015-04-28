package cx;

import java.util.List;
import junit.framework.TestCase;

public class TestContextIterators extends TestCase {

	public void testFor() {
		{// for
			Context cx = new Context();
			cx.evaluate((new Parser("array = [0,1,2,3]; sum=0; for(i=0;i<array.length;i++){array[i]++;sum+=array[i];}")).parse());
			@SuppressWarnings("rawtypes")
			List arr = (List) cx.get("array");
			assertEquals(4, arr.size());
			assertEquals(1L, arr.get(0));
			assertEquals(2L, arr.get(1));
			assertEquals(3L, arr.get(2));
			assertEquals(4L, arr.get(3));
			assertEquals(10L, cx.get("sum"));
		}
		{
			Context cx = new Context();
			Parser parser = new Parser();
			cx.evaluate((parser.parse("s=0; obj = {a:0,b:1,c:2,d:3}; for(e:obj){s+=obj[e];}")));
			assertEquals(6L, cx.get("s"));
		}
		{// foreach array
			Context cx = new Context();
			cx.evaluate((new Parser("s=0; array = [0,1,2,3]; for(e:array){s+=e;}")).parse());
			assertEquals(6L, cx.get("s"));
		}
		{// foreach object
			Context cx = new Context();
			cx.set("i", Integer.valueOf(0xCAFE));
			cx.evaluate((new Parser("s=0; obj = {a:0,b:1,c:2,d:3}; for(e:obj){s+=obj[e];}")).parse());
			assertEquals(6L, cx.get("s"));
			cx.evaluate((new Parser("s+=obj['d'];")).parse());
			assertEquals(9L, cx.get("s"));
		}
		{// do while
			Context cx = new Context();
			Parser parser = new Parser();
			cx.evaluate((parser.parse("s=0; do s++; while(s<20);")));
			assertEquals(20L, cx.get("s"));
		}
		{// do while
			Context cx = new Context();
			Parser parser = new Parser();
			cx.evaluate((parser.parse("s=0; do{ s++; }while(s<20);")));
			assertEquals(20L, cx.get("s"));
		}
		{// do while
			Context cx = new Context();
			Parser parser = new Parser();
			cx.evaluate((parser.parse("s=0; do while(s++<20);")));
			assertEquals(21L, cx.get("s"));
		}
		{// do while
			Context cx = new Context();
			Parser parser = new Parser();
			cx.evaluate((parser.parse("s=0; do while(++s<20);")));
			assertEquals(20L, cx.get("s"));
		}
		{// while do
			Context cx = new Context();
			Parser parser = new Parser();
			cx.evaluate((parser.parse("s=0; while(s<20) s++;")));
			assertEquals(20L, cx.get("s"));
		}
		{// while do
			Context cx = new Context();
			Parser parser = new Parser();
			cx.evaluate((parser.parse("s=0; while(s<20){ s++;}")));
			assertEquals(20L, cx.get("s"));
		}
		{// Knut's [loop while() repeat] structure
			// do { ...; break [condition]; ...; continue [condition]; ...; }
			Context cx = new Context();
			Parser parser = new Parser();
			cx.evaluate((parser.parse("s=0; do{ s++; break s>20;}")));
			assertEquals(21L, cx.get("s"));
		}
		{// Knut's [loop while() repeat] structure
			// do { ...; break [condition]; ...; continue [condition]; ...; }
			Context cx = new Context();
			Parser parser = new Parser();
			cx.evaluate((parser.parse("s=0; do{ s++; break (s>20);}")));
			assertEquals(21L, cx.get("s"));
		}
		{// Knut's [loop while() repeat] structure
			// do { ...; break [condition]; ...; continue [condition]; ...; }
			Context cx = new Context();
			Parser parser = new Parser();
			cx.evaluate((parser.parse("s=0; do{ s+=2; continue s<21; s=21;} while(s!=21 && s<40);")));
			assertEquals(21L, cx.get("s"));
		}
	}
}
