package cx.util.collection;

public abstract interface Stack {
	public abstract void push(StackElement paramStackElement);

	public abstract StackElement pop();

	public abstract StackElement peek();

	public abstract boolean isEmpty();
}