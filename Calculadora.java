public class Calculadora {

    public void analisarRemap(Carro carro, int estagio) {
        double porcentagemGanho = 0.0;
        double precoEstimado = 0.0;
        String valeAPena = "";

        // Regra para motores sem Turbo (Aspirados)
        if (!carro.isTemTurbo()) {
            if (estagio == 1) {
                porcentagemGanho = 0.08;
                precoEstimado = 1300.0;
                valeAPena = "BAIXA";
            } else if (estagio == 2) {
                porcentagemGanho = 0.13;
                precoEstimado = 3800.0;
                valeAPena = "BAIXA";
            } else { // Stage 3
                porcentagemGanho = 0.40;
                precoEstimado = 18000.0;
                valeAPena = "MÉDIA";
            }
        }
        // Regra para motores com Turbo
        else {
            if (estagio == 1) {
                porcentagemGanho = 0.25;
                precoEstimado = 2300.0;
                valeAPena = "ALTA";
            } else if (estagio == 2) {
                porcentagemGanho = 0.35;
                precoEstimado = 5800.0;
                valeAPena = "ALTA";
            } else { // Stage 3
                porcentagemGanho = 0.60;
                precoEstimado = 22000.0;
                valeAPena = "MÉDIA";
            }
        }

        // Cálculos dos ganhos
        int cvGanho = (int) Math.round(carro.getCvOriginal() * porcentagemGanho);
        double torqueGanho = Math.round((carro.getTorqueOriginal() * porcentagemGanho) * 10.0) / 10.0;
        int cvFinal = carro.getCvOriginal() + cvGanho;

        // Imprime o resultado formatado no console
        System.out.println("----------------------------------------------");
        System.out.println("Carro: " + carro.getModelo());
        System.out.println("Estágio Escolhido: Stage " + estagio);
        System.out.println("Potência: " + carro.getCvOriginal() + " CV -> " + cvFinal + " CV (+" + cvGanho + " CV)");
        System.out.println("Torque Ganho: +" + torqueGanho + " Kgfm");
        System.out.println("Preço Estimado: R$ " + String.format("%.2f", precoEstimado));
        System.out.println("Viabilidade: " + valeAPena);
        System.out.println("----------------------------------------------\n");
    }
}