package cx.util.collection;

public abstract interface StackElement {
	public abstract StackElement getNext();

	public abstract void setNext(StackElement paramStackElement);
}