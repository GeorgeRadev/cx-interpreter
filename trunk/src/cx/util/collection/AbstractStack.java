package cx.util.collection;

public class AbstractStack implements Stack {
	protected StackElement top = null;

	public void push(StackElement paramStackElement) {
		paramStackElement.setNext(top);
		top = paramStackElement;
	}

	public StackElement pop() {
		StackElement localStackElement = null;
		if (!isEmpty()) {
			localStackElement = top;
			top = top.getNext();
		}
		return localStackElement;
	}

	public StackElement peek() {
		return top;
	}

	public boolean isEmpty() {
		return top == null;
	}
}