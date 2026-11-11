public class Carro {
    private String modelo;
    private String tipo; // "CAMIONETE" ou "ESPORTIVO"
    private int cvOriginal;
    private double torqueOriginal;
    private boolean temTurbo;

    // Construtor para criar um novo carro
    public Carro(String modelo, String tipo, int cvOriginal, double torqueOriginal, boolean temTurbo) {
        this.modelo = modelo;
        this.tipo = tipo;
        this.cvOriginal = cvOriginal;
        this.torqueOriginal = torqueOriginal;
        this.temTurbo = temTurbo;
    }

    // Getters para pegar os dados
    public String getModelo() {
        return modelo;
    }

    public String getTipo() {
        return tipo;
    }

    public int getCvOriginal() {
        return cvOriginal;
    }

    public double getTorqueOriginal() {
        return torqueOriginal;
    }

    public boolean isTemTurbo() {
        return temTurbo;
    }
}