public class Main {
    public static void main(String[] args) {
        // Criando alguns carros para teste
        Carro hilux = new Carro("Toyota Hilux 2.8 Turbo Diesel", "CAMIONETE", 204, 51.0, true);
        Carro golf = new Carro("VW Golf GTI 2.0 TSI", "ESPORTIVO", 230, 35.7, true);
        Carro sandero = new Carro("Renault Sandero R.S. 2.0", "ESPORTIVO", 150, 20.9, false);

        // Criando a calculadora
        Calculadora calc = new Calculadora();

        // Rodando testes
        System.out.println("=== TESTE DE ANÁLISE DE REMAP ===");
        calc.analisarRemap(golf, 2); // Golf GTI em Stage 2
        calc.analisarRemap(hilux, 1); // Hilux em Stage 1
        calc.analisarRemap(sandero, 1); // Sandero em Stage 1 (Aspirado)
    }
}