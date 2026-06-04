package ec.edu.fica.verify;

public class ContribuyenteNoEncontradoException extends RuntimeException {
    public ContribuyenteNoEncontradoException(String message) { super(message); }
}

class NoEsPersonaNaturalException extends RuntimeException {
    public NoEsPersonaNaturalException(String message) { super(message); }
}
