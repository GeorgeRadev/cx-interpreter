package cx.handlers;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import cx.Context;
import cx.ast.Visitor;
import cx.runtime.Handler;

/**
 * ObjectHandler can have multiple registered instances object instances. <br/>
 * NB: does not handles overwritten methods
 */
public class ObjectHandler implements Handler {
	private static final Map<String, ObjectHandler> supportedObjects = new HashMap<String, ObjectHandler>();
	private static final Map<Object, Map<String, Method>> supportedObjectsMethods = new HashMap<Object, Map<String, Method>>();

	// current registered name in context
	private final Object thizz;

	public ObjectHandler(Object thizz, String name) {
		this.thizz = thizz;
		supportedObjects.put(name, this);

		// get all public methods and cache them
		Method[] methods = thizz.getClass().getMethods();
		Map<String, Method> methodsMap = new HashMap<String, Method>(methods.length + 8);
		for (Method method : methods) {
			methodsMap.put(method.getName(), method);
		}
		supportedObjectsMethods.put(thizz, methodsMap);
	}

	@Override
	public void init(Visitor cx) {
		for (Entry<String, ObjectHandler> supportedObject : supportedObjects.entrySet()) {
			cx.set(supportedObject.getKey(), supportedObject.getValue());
		}
	}

	@Override
	public String[] supportedStaticCalls() {
		return null;
	}

	@Override
	public Object staticCall(String method, Object[] args) {
		return null;
	}

	@Override
	public void set(Object object, String variable, Object value) {}

	private static class ClassMethodCall {
		private final Object thizz;
		private final Method method;

		ClassMethodCall(Object thizz, Method method) {
			this.thizz = thizz;
			this.method = method;
		}
	}

	@Override
	public Object get(Object object, String variable) {
		if (!(object instanceof ObjectHandler)) {
			return null;
		}
		final Object thizz = ((ObjectHandler) object).thizz;
		final Map<String, Method> methodsMap = supportedObjectsMethods.get(thizz);
		if (methodsMap == null) {
			return null;
		}
		Method method = methodsMap.get(variable);
		if (method != null) {
			return new ClassMethodCall(thizz, method);
		}
		return null;
	}

	@Override
	public Object[] supportedClasses() {
		return new Object[] { ObjectHandler.class, ClassMethodCall.class };
	}

	@Override
	public Object call(final Object object, final Object[] args) {
		if (!(object instanceof ClassMethodCall)) {
			return null;
		}
		final ClassMethodCall call = (ClassMethodCall) object;
		final Class<?>[] parameters = call.method.getParameterTypes();

		if (parameters.length != args.length) {
			return null;
		}

		Object result;
		if (parameters.length <= 0) {
			try {
				result = call.method.invoke(call.thizz);
			} catch (Exception e) {
				result = null;
			}
		} else {
			Object[] arguments = new Object[parameters.length];
			for (int i = 0, l = parameters.length; i < l; ++i) {
				arguments[i] = convetType(parameters[i], args[i]);
			}

			try {
				result = call.method.invoke(call.thizz, arguments);
			} catch (Exception e) {
				result = null;
			}
		}
		return result;
	}

	/**
	 * Convert value to a class type
	 * 
	 * @param type
	 * @param value
	 * @return
	 */
	private static Object convetType(final Class<?> type, Object value) {
		if (value == null) {
			return null;
		} else if (type.isAssignableFrom(String.class)) {
			return value.toString();
		} else if (type.isAssignableFrom(long.class) || type.isAssignableFrom(Long.class)) {
			return Context.toLong(value);
		} else if (type.isAssignableFrom(double.class) || type.isAssignableFrom(Double.class)) {
			return Context.toDouble(value);
		} else if (type.isAssignableFrom(boolean.class) || type.isAssignableFrom(Boolean.class)) {
			return Context.isTrue(value);
		} else if (type.isAssignableFrom(int.class) || type.isAssignableFrom(Integer.class)) {
			return Context.toLong(value).intValue();
		} else if (type.isAssignableFrom(float.class) || type.isAssignableFrom(Float.class)) {
			return (float) Context.toDouble(value);
		} else if (type.isAssignableFrom(value.getClass())) {
			return value;
		} else {
			return null;
		}
	}
}
