package br.eti.rslemos.podoscopista;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import br.eti.rslemos.podoscopista.data.Sample;
import br.eti.rslemos.podoscopista.data.Sample.Exception;

public class FootprintRunnerUnitTest {
	@Test
	public void testEmptyClass() {
		List<Sample> samples = new FootprintRunner().run(EmptyClass.class);

		assertThat(samples.size(), is(equalTo(0)));
	}

	@Test
	public void testOneFootprintWithNoArguments() {
		List<Sample> samples = new FootprintRunner().run(OneFootprintWithNoArguments.class);
		
		assertThat(samples.size(), is(equalTo(1)));
		
		Sample sample0 = samples.get(0);
		
		assertThat(sample0.className, is(equalTo(OneFootprintWithNoArguments.class.getName())));
		assertThat(sample0.methodName, is(equalTo("method")));
		assertThat(sample0.methodDescription, is(equalTo("description for the sole footprint method")));
		assertThat(sample0.parameters.length, is(equalTo(0)));
		assertThat(sample0.exception, is(nullValue(Sample.Exception.class)));
		assertThat(sample0.size, is(equalTo(16L)));
		
	}

	@Test
	public void testOneFootprintWith1ArgumentAnd1Datapoint() {
		List<Sample> samples = new FootprintRunner().run(OneFootprintWith1ArgumentAnd1Datapoint.class);
		
		assertThat(samples.size(), is(equalTo(1)));
		
		Sample sample0 = samples.get(0);
		
		assertThat(sample0.className, is(equalTo(OneFootprintWith1ArgumentAnd1Datapoint.class.getName())));
		assertThat(sample0.methodName, is(equalTo("method")));
		assertThat(sample0.methodDescription, is(equalTo("description for the sole footprint method")));
		assertThat(sample0.parameters.length, is(equalTo(1)));
		assertThat(sample0.parameters[0], is(equalTo((Object)OneFootprintWith1ArgumentAnd1Datapoint.D1)));
		assertThat(sample0.exception, is(nullValue(Sample.Exception.class)));
		assertThat(sample0.size, is(equalTo(16L)));
	}

	@Test
	public void testOneFootprintWith1ArgumentAnd2Datapoints() {
		List<Sample> samples = new FootprintRunner().run(OneFootprintWith1ArgumentAnd2Datapoints.class);
		
		assertThat(samples.size(), is(equalTo(2)));
		
		Sample sample0 = samples.get(0);
		assertThat(sample0.className, is(equalTo(OneFootprintWith1ArgumentAnd2Datapoints.class.getName())));
		assertThat(sample0.methodName, is(equalTo("method")));
		assertThat(sample0.methodDescription, is(equalTo("description for the sole footprint method")));
		assertThat(sample0.parameters.length, is(equalTo(1)));
		assertThat(sample0.parameters[0], is(equalTo((Object)OneFootprintWith1ArgumentAnd2Datapoints.D1)));
		assertThat(sample0.exception, is(nullValue(Sample.Exception.class)));
		assertThat(sample0.size, is(equalTo(16L)));
		
		Sample sample1 = samples.get(1);
		assertThat(sample1.className, is(equalTo(OneFootprintWith1ArgumentAnd2Datapoints.class.getName())));
		assertThat(sample1.methodName, is(equalTo("method")));
		assertThat(sample1.methodDescription, is(equalTo("description for the sole footprint method")));
		assertThat(sample1.parameters.length, is(equalTo(1)));
		assertThat(sample1.parameters[0], is(equalTo((Object)OneFootprintWith1ArgumentAnd2Datapoints.D2)));
		assertThat(sample1.exception, is(nullValue(Sample.Exception.class)));
		assertThat(sample1.size, is(equalTo(16L)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testOneFootprintWith1ArgumentAndNoDatapoint() {
		new FootprintRunner().run(OneFootprintWith1ArgumentAndNoDatapoint.class);
	}

	@Test
	public void testTwoFootprintsWith2ArgumentsAnd2Datapoints() {
		List<Sample> samples = new FootprintRunner().run(TwoFootprintsWith2ArgumentsAnd2Datapoints.class);
		
		assertThat(samples.size(), is(equalTo(8)));
	}

	@Test
	public void testTwoFootprintsWith2DifferentArgumentsAnd2Datapoints() {
		List<Sample> samples = new FootprintRunner().run(TwoFootprintsWith2DifferentArgumentsAnd2Datapoints.class);
		
		assertThat(samples.size(), is(equalTo(2)));
	}

	@Test
	public void testFootprintThrowsException() {
		List<Sample> samples = new FootprintRunner().run(FootprintThrowsException.class);
		
		assertThat(samples.size(), is(equalTo(1)));
		Sample sample0 = samples.get(0);
		assertThat(sample0.className, is(equalTo(FootprintThrowsException.class.getName())));
		assertThat(sample0.methodName, is(equalTo("method")));
		assertThat(sample0.methodDescription, is(equalTo("description for the sole footprint method")));
		assertThat(sample0.parameters.length, is(equalTo(0)));
		assertThat(sample0.size, is(equalTo(-1L)));
		assertThat(sample0.exception, is(not(nullValue(Exception.class))));
		assertThat(sample0.exception.className, is(equalTo(RuntimeException.class.getName())));
		assertThat(sample0.exception.message, is(nullValue(String.class)));
		assertThat(sample0.exception.stackTrace, is(not(nullValue(StackTraceElement[].class))));
		
		StackTraceElement firstFrame = sample0.exception.stackTrace[0];
		assertThat(firstFrame.getClassName(), is(equalTo(FootprintThrowsException.class.getName())));
		assertThat(firstFrame.getFileName(), is(equalTo("FootprintRunnerUnitTest.java")));
		assertThat(firstFrame.getMethodName(), is(equalTo("method")));
		
	}

	public static class EmptyClass {
		public Object thisClassHasAMethodThatNeverGetsCalled() {
			fail("unexpected call");
			return new Object();
		}
	}
	
	public static class OneFootprintWithNoArguments {
		@Footprint("description for the sole footprint method")
		public Object method() {
			return new Object();
		}
	}
	
	public static class OneFootprintWith1ArgumentAndNoDatapoint {
		public static final int NOT_USED = -1;
		
		@Footprint("description for the sole footprint method")
		public Object method(int x) {
			return new Object();
		}
	}
	
	public static class OneFootprintWith1ArgumentAnd1Datapoint {
		public static final int NOT_USED = -1;
		@Datapoint public static final int D1 = 0;
		
		@Footprint("description for the sole footprint method")
		public Object method(int x) {
			return new Object();
		}
	}
	
	public static class OneFootprintWith1ArgumentAnd2Datapoints {
		public static final int NOT_USED = -1;
		@Datapoint public static final int D1 = 0;
		@Datapoint public static final int D2 = 10;
		
		@Footprint("description for the sole footprint method")
		public Object method(int x) {
			return new Object();
		}
	}
	
	public static class TwoFootprintsWith2ArgumentsAnd2Datapoints {
		public static final int NOT_USED = -1;
		@Datapoint public static final int D1 = 0;
		@Datapoint public static final int D2 = 10;
		
		@Footprint("description for the first footprint method")
		public Object method1(int x, int y) {
			return new Object();
		}
		
		@Footprint("description for the second footprint method")
		public Object method2(int x, int y) {
			return new Object();
		}
	}
	
	public static class TwoFootprintsWith2DifferentArgumentsAnd2Datapoints {
		public static final int NOT_USED = -1;
		@Datapoint public static final int D1 = 0;
		@Datapoint public static final long D2 = 10;
		
		@Footprint("description for the first footprint method")
		public Object method1(int x, long y) {
			return new Object();
		}
		
		@Footprint("description for the second footprint method")
		public Object method2(int x, long y) {
			return new Object();
		}
	}

	public static class FootprintThrowsException {
		@Footprint("description for the sole footprint method")
		public Object method() {
			throw new RuntimeException();
		}
	}
}


