package smk.adzikro.indextemaquran.interfaces;

public interface Presenter<T> {
  void bind(T what);
  void unbind(T what);
}
