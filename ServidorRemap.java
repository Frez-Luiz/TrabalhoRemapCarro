import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

public class ServidorRemap {
    private JFrame janela;
    private HttpServer server;
    private JTextArea logArea;
    private JButton btnIniciar;
    private JButton btnParar;
    private JLabel statusLabel;

    // Campos de cadastro Swing
    private JTextField txtNome, txtMotor, txtTracao, txtPeso;
    private JTextField txtCvOrig, txtCvStg1, txtCvStg2, txtCvStg3, txtCvStg4;
    private JTextField txtTqOrig, txtTqStg1, txtTqStg2, txtTqStg3, txtTqStg4;
    private JComboBox<String> cbCategoria;
    private JCheckBox chkCompensa;
    private JTextArea txtMotivo, txtSetup;

    private static final Map<String, Veiculo> bancoDeVeiculos = new LinkedHashMap<>();

    static class Veiculo {
        String id, nome, categoria, motor, tracao;
        int pesoKg;
        int cvOrig, cvStg1, cvStg2, cvStg3, cvStg4;
        double tqOrig, tqStg1, tqStg2, tqStg3, tqStg4;
        boolean compensa;
        String motivo, recomendacao;

        public Veiculo(String id, String nome, String categoria, String motor, String tracao, int pesoKg,
                int cvOrig, int cvStg1, int cvStg2, int cvStg3, int cvStg4,
                double tqOrig, double tqStg1, double tqStg2, double tqStg3, double tqStg4,
                boolean compensa, String motivo, String recomendacao) {
            this.id = id;
            this.nome = nome;
            this.categoria = categoria;
            this.motor = motor;
            this.tracao = tracao;
            this.pesoKg = pesoKg;
            this.cvOrig = cvOrig;
            this.cvStg1 = cvStg1;
            this.cvStg2 = cvStg2;
            this.cvStg3 = cvStg3;
            this.cvStg4 = cvStg4;
            this.tqOrig = tqOrig;
            this.tqStg1 = tqStg1;
            this.tqStg2 = tqStg2;
            this.tqStg3 = tqStg3;
            this.tqStg4 = tqStg4;
            this.compensa = compensa;
            this.motivo = motivo;
            this.recomendacao = recomendacao;
        }

        private String escaparJson(String texto) {
            return texto.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
        }

        public String toJson() {
            return String.format(java.util.Locale.US,
                    "{\"id\":\"%s\",\"nome\":\"%s\",\"categoria\":\"%s\",\"motor\":\"%s\"," +
                            "\"tracao\":\"%s\",\"pesoKg\":%d," +
                            "\"cvOrig\":%d,\"cvStg1\":%d,\"cvStg2\":%d,\"cvStg3\":%d,\"cvStg4\":%d," +
                            "\"tqOrig\":%.1f,\"tqStg1\":%.1f,\"tqStg2\":%.1f,\"tqStg3\":%.1f,\"tqStg4\":%.1f," +
                            "\"compensa\":%b,\"motivo\":\"%s\",\"recomendacao\":\"%s\"}",
                    escaparJson(id), escaparJson(nome), escaparJson(categoria), escaparJson(motor),
                    escaparJson(tracao), pesoKg,
                    cvOrig, cvStg1, cvStg2, cvStg3, cvStg4,
                    tqOrig, tqStg1, tqStg2, tqStg3, tqStg4,
                    compensa, escaparJson(motivo), escaparJson(recomendacao));
        }
    }

    public ServidorRemap() {
        inicializarDados();

        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("[AVISO] Modo Terminal (Headless) ativado.");
            iniciarServidor();
        } else {
            configurarInterface();
        }
    }

    private void add(String id, String nome, String cat, String motor, String tracao, int peso,
            int c0, int c1, int c2, int c3, int c4,
            double t0, double t1, double t2, double t3, double t4,
            boolean comp, String mot, String rec) {
        bancoDeVeiculos.put(id, new Veiculo(id, nome, cat, motor, tracao, peso, c0, c1, c2, c3, c4, t0, t1, t2, t3, t4,
                comp, mot, rec));
    }

    private void inicializarDados() {
        // ==================== 25 CAMIONETES ====================
        add("hilux_28", "Toyota Hilux 2.8 Turbo Diesel", "Camionete", "2.8 16V 1GD-FTV Turbo Diesel", "4x4", 2090,
                204, 255, 285, 340, 410, 50, 62, 69, 80, 95, true,
                "Bloco diesel muito robusto. Stage 1 e 2 entregam +12 a +19 kgfm de torque sem comprometer a durabilidade.",
                "Stage 2: Downpipe inox 3\" + Filtro inbox + Remap motor/câmbio.");
        add("hilux_grs", "Toyota Hilux GR-Sport 2.8", "Camionete", "2.8 Turbo Diesel Calibrado", "4x4", 2130,
                224, 265, 295, 350, 420, 55, 65, 72, 83, 98, true,
                "Já vem com mapa mais forte de fábrica, mas ainda possui boa margem térmica para downpipe e remap.",
                "Stage 2 + Upgrade de intercooler para manter temperatura baixa na terra.");
        add("amarok_v6", "VW Amarok V6 3.0 TDI", "Camionete", "3.0 V6 Turbo Diesel", "4Motion Integral", 2180,
                258, 318, 355, 420, 490, 59, 72, 80, 92, 105, true,
                "Melhor motor diesel médio para preparação. Tração integral garante arrancadas absurdas no asfalto.",
                "Stage 2 + Remap de câmbio ZF8 para trocas mais rápidas.");
        add("amarok_20", "VW Amarok 2.0 Bi-Turbo Diesel", "Camionete", "2.0 Bi-TDI Diesel", "4x4", 2050,
                180, 220, 245, 285, 330, 42, 51, 56, 64, 73, true,
                "Sistema biturbo sequencial responde rápido em baixa rotação após calibração eletrônica.",
                "Stage 1 ou 2 conservador + Revisão preventiva da correia dentada.");
        add("ranger_v6", "Ford Ranger Limited 3.0 V6 Diesel", "Camionete", "3.0 V6 Lion Turbo Diesel", "4WD", 2350,
                250, 305, 340, 400, 460, 61, 73, 81, 92, 104, true,
                "Motor V6 moderno com excelente curva de torque. Melhora retomadas e consumo em estrada.",
                "Stage 2 + Calibração da transmissão automática de 10 marchas.");
        add("ranger_raptor", "Ford Ranger Raptor 3.0 V6 Bi-Turbo", "Camionete", "3.0 V6 EcoBoost Gasolina",
                "4WD Permanente", 2415,
                397, 460, 510, 590, 680, 59, 69, 77, 88, 98, true,
                "Motor biturbo a gasolina com bloco reforçado. Ganha +113 cv no Stage 2 com facilidade.",
                "Stage 2 com gasolina Podium + Intercooler frontal maior.");
        add("ranger_32", "Ford Ranger 3.2 5 Cilindros Diesel", "Camionete", "3.2 Duratorq 5 Cil. Turbo", "4x4", 2230,
                200, 245, 275, 320, 375, 48, 58, 65, 74, 85, true,
                "Motor de 5 cilindros com ótimo torque em baixa. Responde muito bem a downpipe e filtro.",
                "Stage 2 + Solução de válvula EGR para evitar carbonização.");
        add("s10_diesel", "Chevrolet S10 2.8 Turbo Diesel", "Camionete", "2.8 Duramax Turbo Diesel", "4x4", 2050,
                200, 248, 278, 330, 390, 51, 61, 68, 78, 90, true,
                "Turbina de geometria variável robusta e bicos com boa vazão original.",
                "Stage 2: Downpipe inox + Filtro esportivo + Remap.");
        add("s10_flex", "Chevrolet S10 2.5 Flex (Aspirada)", "Camionete", "2.5 Ecotec Aspirado", "4x2 / 4x4", 1920,
                206, 216, 224, 245, 290, 27, 29, 31, 35, 42, false,
                "Motor aspirado em veículo pesado: ganho de apenas 10 cv no Stage 1 não compensa o valor.",
                "Não compensa remap. Use apenas módulo de pedal eletrônico.");
        add("ram_2500", "RAM 2500 Laramie 6.7 Cummins", "Camionete", "6.7 6 Cil. Turbo Diesel", "4x4", 3440,
                365, 445, 520, 650, 800, 110, 134, 155, 185, 220, true,
                "Motor de caminhão leve com torque colossal. Remap libera força bruta para reboque.",
                "Stage 2 + Escapamento 4\" + Reforço no conversor de torque.");
        add("ram_3500", "RAM 3500 Limited Longhorn 6.7", "Camionete", "6.7 Cummins High Output", "4x4", 3620,
                377, 460, 540, 680, 840, 117, 142, 165, 195, 230, true,
                "Projetada para cargas extremas. Suporta ganhos massivos de torque com calibração de transmissão.",
                "Stage 2 + Monitoramento de temperatura de gases de escape (EGT).");
        add("ram_1500_trx", "RAM 1500 TRX 6.2 V8 Supercharged", "Camionete", "6.2 V8 HEMI Hellcat", "4WD Integral",
                2900,
                712, 780, 840, 950, 1100, 89, 98, 106, 118, 135, true,
                "Compressor mecânico de fábrica. Troca de polia e mapa ultrapassa 840 cv rapidamente.",
                "Stage 2: Polia menor do supercharger + Bicos maiores + Intake.");
        add("ram_1500_rebel", "RAM 1500 Rebel 5.7 V8 HEMI", "Camionete", "5.7 V8 HEMI Aspirado", "4x4", 2610,
                400, 428, 450, 540, 660, 56, 60, 64, 76, 90, true,
                "V8 de alta cilindrada responde bem a coletor dimensionado e acerto de ponto. Stage 3+ exige Supercharger.",
                "Stage 2 Aspirado: Cold Air Intake + Coletor + Escape inox.");
        add("ram_rampage", "RAM Rampage R/T 2.0 Hurricane 4", "Camionete", "2.0 Turbo Gasolina (GME-T4)", "4x4", 1917,
                272, 325, 355, 415, 480, 40, 48, 53, 61, 70, true,
                "Mesmo motor do Jeep Wrangler. Picape monobloco leve que vira um esportivo no Stage 2.",
                "Stage 2: Downpipe + Filtro esportivo + Remap.");
        add("l200_triton", "Mitsubishi L200 Triton 2.4 Diesel", "Camionete", "2.4 MIVEC Turbo Diesel",
                "Super Select 4x4", 1950,
                190, 232, 258, 300, 350, 44, 53, 59, 68, 78, true,
                "Bloco em alumínio e peso reduzido deixam a picape muito ágil após o Stage 1.",
                "Stage 1 ou 2 conservador para controle térmico do alumínio.");
        add("frontier_23", "Nissan Frontier PRO-4X 2.3 Bi-Turbo", "Camionete", "2.3 Bi-Turbo Diesel", "4x4", 2150,
                190, 235, 260, 305, 355, 45, 55, 61, 70, 80, true,
                "Duas turbinas garantem resposta rápida em baixa rotação e bom ganho de torque.",
                "Stage 2 + Downpipe inox + Filtro inbox.");
        add("toro_diesel", "Fiat Toro Ultra 2.0 TD350", "Camionete", "2.0 Multijet II Turbo Diesel", "4x4", 1870,
                170, 210, 232, 270, 315, 35, 44, 49, 56, 64, true,
                "Leve e ágil. O ganho de +9 kgfm elimina o atraso nas saídas e ultrapassagens.",
                "Stage 2: Downpipe + Filtro inbox + Remap.");
        add("toro_t270", "Fiat Toro / Strada Turbo 270 / 200", "Camionete", "1.3 / 1.0 Turbo Flex (GSE)", "4x2", 1650,
                185, 215, 230, 265, 310, 27, 32, 35, 40, 46, true,
                "Motores turbo modernos da Stellantis respondem bem ao etanol e downpipe.",
                "Stage 2 com mapa exclusivo para Etanol.");
        add("f150_v8", "Ford F-150 Lariat 5.0 V8 Coyote", "Camionete", "5.0 V8 Aspirado (Coyote)", "4x4", 2480,
                405, 435, 460, 620, 780, 56, 61, 65, 82, 98, true,
                "Motor Coyote aceita kit Supercharger (Stage 3/4) transformando a F-150 em um monstro de pista.",
                "Stage 2 Aspirado ou Stage 3 com Supercharger Whipple.");
        add("silverado_v8", "Chevrolet Silverado High Country 5.3 V8", "Camionete", "5.3 V8 EcoTec3 Aspirado", "4x4",
                2510,
                360, 388, 412, 540, 680, 52, 56, 60, 75, 90, true,
                "V8 americano clássico. Stage 1/2 melhora resposta e desativa o corte de cilindros (AFM).",
                "Stage 2 + Desativação eletrônica do gerenciamento ativo de combustível.");
        add("maverick_fx4", "Ford Maverick Lariat FX4 2.0 EcoBoost", "Camionete", "2.0 Turbo Gasolina", "AWD", 1744,
                253, 305, 335, 395, 460, 38, 46, 51, 59, 68, true,
                "Base mecânica do Focus ST. Com tração integral e peso baixo, anda junto com esportivos.",
                "Stage 2: Downpipe + Intercooler + Intake + Remap.");
        add("saveiro_16", "VW Saveiro Cross 1.6 MSI (Aspirada)", "Camionete", "1.6 16V Aspirado", "Dianteira", 1130,
                120, 128, 134, 155, 210, 16, 17, 18, 21, 28, false,
                "Motor aspirado pequeno: remap puro ganha menos de 8 cv. Só compensa se instalar kit turbo.",
                "Não compensa remap aspirado.");
        add("strada_13", "Fiat Strada Volcano 1.3 Firefly (Aspirada)", "Camionete", "1.3 8V Aspirado", "Dianteira",
                1170,
                107, 113, 118, 135, 180, 13.7, 14.5, 15.2, 17.5, 23, false,
                "Motor focado em economia urbana. Ganho insignificante apenas com software.",
                "Não compensa preparar.");
        add("oroch_13t", "Renault Oroch Outsider 1.3 TCe Turbo", "Camionete", "1.3 Turbo Flex (Mercedes/Renault)",
                "Dianteira", 1432,
                170, 202, 220, 255, 300, 27, 32, 35, 40, 46, true,
                "Motor desenvolvido em parceria com a Mercedes-Benz. Excelente resposta no Stage 1 e 2.",
                "Stage 2: Downpipe + Filtro esportivo + Remap.");
        add("troller_t4", "Troller T4 3.2 Turbo Diesel", "Camionete", "3.2 5 Cilindros Duratorq", "4x4 Reduzida", 2140,
                200, 248, 278, 330, 390, 48, 59, 66, 76, 88, true,
                "Jipe raiz. O ganho de torque em baixa rotação facilita muito trilhas pesadas e lama.",
                "Stage 2 + Snorkel + Intercooler reforçado.");

        // ==================== 25 CARROS ESPORTIVOS ====================
        add("golf_gti", "VW Golf GTI MK7 (2.0 TSI)", "Esportivo", "2.0 Turbo (EA888 Gen3)", "Dianteira (FWD)", 1370,
                220, 295, 330, 410, 520, 35, 46, 51, 58, 68, true,
                "Rei do custo-benefício. Stage 2 mantém confiabilidade original e baixa o 0-100 km/h para ~5s.",
                "Stage 2: Downpipe 3\" + Intake + Remap Motor e Câmbio DSG.");
        add("golf_r", "VW Golf R MK8 2.0 TSI", "Esportivo", "2.0 Turbo (EA888 Gen4)", "4Motion (AWD)", 1550,
                320, 395, 430, 510, 620, 42, 52, 57, 65, 76, true,
                "Já vem com turbina maior de fábrica e tração integral com vetorização de torque.",
                "Stage 2 + Intercooler + Remap DSG.");
        add("jetta_gli", "VW Jetta GLI 2.0 TSI (231 cv)", "Esportivo", "2.0 Turbo (EA888 Gen3)", "Dianteira (FWD)",
                1476,
                231, 305, 340, 420, 530, 35, 47, 52, 60, 70, true,
                "Sedã com mecânica completa do Golf GTI. Responde de forma idêntica ao Stage 1 e 2.",
                "Stage 2: Downpipe + Intake + Velas grau 8 + Remap.");
        add("audi_rs3", "Audi RS3 Sedan 2.5 TFSI", "Esportivo", "2.5 5 Cilindros Turbo (DAZA)", "Quattro (AWD)", 1515,
                400, 485, 540, 680, 850, 49, 62, 68, 82, 96, true,
                "5 cilindros icônico com internos resistentes. No Stage 2 E85 anda na frente de supercarros.",
                "Stage 2 E85 ou Stage 3 com turbina híbrida TTE700.");
        add("audi_s3", "Audi S3 Sportback 2.0 TFSI", "Esportivo", "2.0 Turbo (IS38)", "Quattro (AWD)", 1500,
                290, 365, 395, 470, 570, 38, 48, 53, 61, 72, true,
                "Tração Quattro e turbina IS38 de fábrica permitem arrancadas brutais mesmo no Stage 1.",
                "Stage 2: Downpipe + Intercooler + Remap S-Tronic.");
        add("audi_ttrs", "Audi TT RS Coupé 2.5 TFSI", "Esportivo", "2.5 5 Cilindros Turbo", "Quattro (AWD)", 1440,
                400, 490, 550, 690, 860, 49, 63, 70, 84, 98, true,
                "Mais leve que o RS3 com o mesmo conjunto mecânico. Um dos carros mais rápidos de rua do mundo preparado.",
                "Stage 2 E85 + Pneus semi-slick.");
        add("bmw_320i", "BMW 320i G20 (2.0 Turbo)", "Esportivo", "2.0 TwinPower Turbo (B48)", "Traseira (RWD)", 1460,
                184, 265, 295, 360, 440, 30, 42, 47, 55, 64, true,
                "Limitada eletronicamente de fábrica. Stage 1 desbloqueia +81 cv sem abrir o capô.",
                "Stage 1 ou 2 via OBD2. Acima de Stage 3 exige pistões forjados.");
        add("bmw_m2", "BMW M2 Competition (F87)", "Esportivo", "3.0 6 Cil. Bi-Turbo (S55)", "Traseira (RWD)", 1550,
                410, 515, 560, 670, 800, 56, 68, 74, 86, 98, true,
                "Motor S55 derivado da M3/M4. Ganha mais de 100 cv apenas com software.",
                "Stage 2 + Crank Hub reforçado (prevenção obrigatória no S55).");
        add("bmw_m3", "BMW M3 Competition (G80)", "Esportivo", "3.0 6 Cil. Bi-Turbo (S58)", "M xDrive (AWD)", 1730,
                510, 620, 680, 820, 1020, 66, 80, 88, 102, 120, true,
                "Bloco closed-deck forjado de fábrica. Suporta 800+ cv com confiabilidade impressionante.",
                "Stage 2: Downpipes + Midpipe + Remap.");
        add("bmw_m4", "BMW M4 CS / Competition (G82)", "Esportivo", "3.0 6 Cil. Bi-Turbo (S58)", "Traseira / AWD", 1725,
                510, 625, 685, 830, 1040, 66, 81, 89, 104, 122, true,
                "Coupé de pista com arrefecimento superdimensionado. Excelente resposta em qualquer Stage.",
                "Stage 2 com mapa de câmbio ZF8 CS.");
        add("porsche_911", "Porsche 911 Carrera S (992)", "Esportivo", "3.0 Boxer Bi-Turbo", "Traseira (RWD)", 1515,
                450, 535, 575, 660, 780, 54, 65, 71, 80, 92, true,
                "Engenharia alemã de pista. Câmbio PDK aguenta torque alto sem patinar.",
                "Stage 2 com catalisadores esportivos de 200 células.");
        add("porsche_911_turbo", "Porsche 911 Turbo S (992)", "Esportivo", "3.7 Boxer Bi-Turbo VTG", "AWD", 1640,
                650, 750, 810, 940, 1120, 81, 94, 102, 116, 135, true,
                "Turbinas de geometria variável. No Stage 2 faz 0 a 100 km/h em 2,3 segundos.",
                "Stage 2: Escape em titânio/inox + Intercoolers + Remap PDK.");
        add("porsche_cayman_gt4", "Porsche 718 Cayman GT4 4.0", "Esportivo", "4.0 6 Cil. Boxer Aspirado",
                "Traseira (RWD)", 1420,
                420, 442, 460, 500, 560, 43, 45, 47, 51, 57, false,
                "Motor aspirado purista de alta rotação. Ganho pequeno em potência; foco é acerto de ronco e resposta.",
                "Escape completo (Headers) + Remap fino apenas para otimizar curva.");
        add("mercedes_a45", "Mercedes-AMG A45 S 2.0 Turbo", "Esportivo", "2.0 Turbo (M139 Forjado)", "4MATIC+ (AWD)",
                1550,
                421, 485, 520, 610, 720, 51, 59, 64, 73, 84, true,
                "O 2.0 quatro cilindros mais potente do mundo de série, já forjado pela AMG.",
                "Stage 2: Downpipe + Intake + Remap.");
        add("mercedes_c63", "Mercedes-AMG C63 S 4.0 V8 Bi-Turbo", "Esportivo", "4.0 V8 Bi-Turbo (M177)",
                "Traseira (RWD)", 1755,
                510, 600, 645, 760, 900, 71, 84, 91, 105, 120, true,
                "V8 biturbo com turbinas dentro do 'V'. Ganha +135 cv no Stage 2 com facilidade.",
                "Stage 2 + Pneus largos na traseira para controlar o torque.");
        add("civic_type_r", "Honda Civic Type R (FL5)", "Esportivo", "2.0 VTEC Turbo (K20C1)", "Dianteira (FWD)", 1430,
                297, 355, 385, 450, 540, 43, 51, 56, 63, 72, true,
                "O Type R moderno é turbo e responde muito bem a downpipe, intercooler e calibração.",
                "Stage 2: Downpipe + Intercooler frontal + Intake.");
        add("civic_si", "Honda Civic Si 2.0 (K20 Aspirado)", "Esportivo", "2.0 i-VTEC Aspirado", "Dianteira (FWD)",
                1320,
                192, 204, 218, 250, 340, 19, 20, 22, 25, 36, false,
                "Aspirado de 8.000 rpm: remap puro ganha pouco. Só compensa em projeto completo aspirado ou turbo.",
                "Coletor 4x2x1 + Escape + Hondata FlashPro.");
        add("civic_touring", "Honda Civic Touring 1.5 Turbo", "Esportivo", "1.5 VTEC Turbo (L15B7)", "Dianteira (FWD)",
                1326,
                173, 212, 232, 275, 330, 22.4, 28, 31, 36, 43, true,
                "Excelente ganho no Stage 1/2, mas exige cuidado com o limite de torque do câmbio CVT.",
                "Stage 1 ou 2 com mapa de torque progressivo para proteger o CVT.");
        add("mustang_gt", "Ford Mustang GT 5.0 V8 (Coyote)", "Esportivo", "5.0 V8 Aspirado", "Traseira (RWD)", 1780,
                466, 495, 520, 680, 850, 56, 60, 64, 78, 94, true,
                "Responde bem no Stage 2 com admissão. No Stage 3/4 recebe Supercharger passando de 700 cv.",
                "Stage 2: Cold Air Intake + Coletor GT350 + Remap.");
        add("camaro_ss", "Chevrolet Camaro SS 6.2 V8", "Esportivo", "6.2 V8 LT1 Aspirado", "Traseira (RWD)", 1709,
                461, 488, 512, 660, 820, 62.9, 66.5, 70, 85, 100, true,
                "V8 de grande litragem com torque imediato. Stage 3+ utiliza Supercharger Magnuson/Whipple.",
                "Stage 2 Aspirado: Headers (coletor longo) + Intake + Remap.");
        add("polo_gts", "VW Polo GTS 1.4 TSI", "Esportivo", "1.4 Turbo (EA211)", "Dianteira (FWD)", 1213,
                150, 185, 205, 245, 300, 25, 32, 35, 40, 47, true,
                "Carro leve que fica muito ágil no Stage 1 e 2 sem forçar o câmbio automático.",
                "Stage 2: Downpipe + Filtro inbox + Remap.");
        add("up_tsi", "VW Up! TSI 1.0 Turbo (Stage Pocket)", "Esportivo", "1.0 3 Cilindros Turbo", "Dianteira (FWD)",
                951,
                105, 135, 152, 185, 230, 16.8, 22, 25, 29, 35, true,
                "Com apenas 951 kg, o Stage 2 deixa a relação peso/potência melhor que a de muitos sedãs 2.0.",
                "Stage 2: Downpipe + Filtro + Velas mais frias + Remap.");
        add("lancer_evo", "Mitsubishi Lancer Evolution X", "Esportivo", "2.0 Turbo (4B11T)", "S-AWC (AWD)", 1560,
                295, 345, 380, 460, 580, 37, 45, 50, 58, 70, true,
                "Ícone do rali. Bloco 4B11T suporta bem até Stage 2/3; acima disso exige camisas reforçadas.",
                "Stage 2: Escape 3\" + Intake + Solenoide de boost + Remap.");
        add("subaru_wrx", "Subaru WRX STI 2.5 Boxer Turbo", "Esportivo", "2.5 Boxer Turbo (EJ257)", "Symmetrical AWD",
                1530,
                310, 355, 390, 470, 600, 40, 47, 52, 61, 75, true,
                "Tração integral simétrica imbatível em curvas. Exige controle rigoroso de temperatura e pistões no Stage 3+.",
                "Stage 2: Downpipe + Bomba de combustível + Acerto fino de mistura.");
        add("nissan_gtr", "Nissan GT-R R35 3.8 V6 Bi-Turbo", "Esportivo", "3.8 V6 Bi-Turbo (VR38DETT)",
                "ATTESA E-TS (AWD)", 1752,
                572, 650, 710, 880, 1200, 64.5, 76, 84, 102, 130, true,
                "O Godzilla da preparação. Stage 2 já entra na casa dos 700 cv; Stage 4 passa facilmente de 1.000 cv.",
                "Stage 2: Downpipes + Midpipe + Bicos + Bomba + Remap de câmbio GR6.");

        // ==================== 8 MOTOS ESPORTIVAS ====================
        add("s1000rr", "BMW S 1000 RR (Superbike)", "Moto Esportiva", "999cc 4 Cil. ShiftCam", "Corrente (RWD)", 197,
                210, 222, 232, 248, 275, 11.5, 12.2, 12.8, 13.6, 15.0, true,
                "O remap remove as restrições eletrônicas de acelerador nas primeiras marchas e libera o giro máximo.",
                "Stage 2: Escapamento Full Titânio (Akrapovic) + Filtro Sprint + Remap ECU.");
        add("panigale_v4", "Ducati Panigale V4 S", "Moto Esportiva", "1.103cc V4 Desmosedici Stradale",
                "Corrente (RWD)", 195,
                215, 228, 238, 255, 280, 12.6, 13.4, 14.0, 15.0, 16.5, true,
                "Moto de MotoGP para rua. Com escape full e mapa Ducati Performance a entrega de potência fica brutal.",
                "Stage 2: Sistema Full Akrapovic + Filtro racing + Calibração de controle de tração.");
        add("r1_yamaha", "Yamaha YZF-R1 (Crossplane)", "Moto Esportiva", "998cc 4 Cil. Crossplane", "Corrente (RWD)",
                201,
                200, 211, 220, 235, 260, 11.5, 12.1, 12.7, 13.5, 14.8, true,
                "Virabrequim crossplane com ronco único. Remap suaviza o freio-motor e tira o limitador de 299 km/h.",
                "Stage 2: Eliminador de catalisador (Y-Pipe) + Ponteira + Filtro K&N + Remap.");
        add("zx10r", "Kawasaki Ninja ZX-10R", "Moto Esportiva", "998cc 4 Cilindros em Linha", "Corrente (RWD)", 207,
                203, 215, 225, 240, 265, 11.7, 12.4, 13.0, 13.8, 15.2, true,
                "Vem bastante restrita de fábrica por normas de ruído e emissões. Ganha muita força em média/alta rotação.",
                "Stage 2: Coletor inox + Filtro esportivo + Remap.");
        add("cbr1000rrr", "Honda CBR 1000RR-R Fireblade SP", "Moto Esportiva", "1.000cc 4 Cil. Alta Rotação",
                "Corrente (RWD)", 201,
                217, 229, 239, 255, 280, 11.5, 12.2, 12.8, 13.6, 15.0, true,
                "Moto extremamente focada em pista. O remap melhora o torque em médias rotações para uso na rua.",
                "Stage 2: Escape Full + Filtro esportivo + Acerto de Quickshifter.");
        add("mt09", "Yamaha MT-09 (Master of Torque)", "Moto Esportiva", "890cc 3 Cilindros CP3", "Corrente (RWD)", 189,
                119, 126, 133, 145, 165, 9.5, 10.1, 10.7, 11.6, 13.0, true,
                "Naked leve com muito torque. Remap elimina o tranco do acelerador eletrônico (on/off) e libera potência.",
                "Stage 2: Escapamento Full 3x1 + Filtro esportivo + Remap.");
        add("hornet_cb650r", "Honda CB 650R / CBR 650R", "Moto Esportiva", "649cc 4 Cilindros em Linha",
                "Corrente (RWD)", 202,
                88, 94, 99, 108, 125, 6.1, 6.5, 6.9, 7.5, 8.6, true,
                "Quatro cilindros aspirado: ganho de 6 a 11 cv, mas melhora muito o ronco e a resposta do acelerador.",
                "Stage 2: Escapamento Full 4x1 + Filtro K&N + Remap.");
        add("ninja_400", "Kawasaki Ninja 400 (Bimestral/Pista)", "Moto Esportiva", "399cc Bicilíndrica",
                "Corrente (RWD)", 168,
                48, 52, 55, 62, 72, 3.8, 4.1, 4.4, 4.9, 5.6, false,
                "Por ter baixa cilindrada aspirada, o ganho em cavalos é pequeno (4 a 7 cv). Só vale para competição na categoria.",
                "Filtro esportivo + Ponteira + Remap apenas se correr em track days.");

        // ==================== 7 MOTOS DE TRILHA ====================
        add("crf_250f", "Honda CRF 250F (Trilha Nacional)", "Moto de Trilha", "249cc Monocilíndrico OHC",
                "Corrente Off-Road", 114,
                22, 24, 26, 30, 36, 2.2, 2.4, 2.6, 3.0, 3.5, true,
                "A rainha das trilhas no Brasil. Remap + escapamento melhora muito a subida de morros e tira o corte baixo.",
                "Stage 2: Curva e ponteira esportiva (Foco/Belparts) + Filtro Twin Air + Remap.");
        add("crf_450r", "Honda CRF 450R / RX (Motocross/Enduro)", "Moto de Trilha", "449cc Unicam Alta Compressão",
                "Corrente Off-Road", 111,
                55, 59, 63, 69, 76, 4.9, 5.3, 5.7, 6.2, 6.8, true,
                "Moto de competição pura com 111 kg. O remap permite personalizar os 3 mapas de entrega de potência para barro ou seco.",
                "Stage 2: Escapamento Full + Acerto de mapas de ignição e controle de largada.");
        add("ktm_350_excf", "KTM 350 EXC-F (Enduro Importada)", "Moto de Trilha", "350cc DOHC Injeção Eletrônica",
                "Corrente Off-Road", 104,
                46, 50, 54, 60, 67, 4.1, 4.5, 4.8, 5.3, 5.9, true,
                "Considerada a cilindrada perfeita para enduro: leve como uma 250 e forte como uma 450.",
                "Stage 2: Remoção de restrições de admissão + Escape FMF + Remap (GET/Vortex).");
        add("ktm_450_sxf", "KTM 450 SX-F Factory Edition", "Moto de Trilha", "450cc SOHC Competição",
                "Corrente Off-Road", 102,
                63, 67, 71, 78, 86, 5.4, 5.8, 6.2, 6.8, 7.5, true,
                "Relação peso/potência absurda (1,5 kg/cv). Calibração eletrônica deixa a entrega mais dócil ou explosiva.",
                "Stage 2: Sistema Akrapovic + Central programável.");
        add("yz_250f", "Yamaha YZ 250F / WR 250F", "Moto de Trilha", "250cc Cabeçote Invertido", "Corrente Off-Road",
                106,
                42, 45, 48, 53, 60, 2.9, 3.2, 3.4, 3.8, 4.3, true,
                "Admissão frontal e cilindro inclinado para trás. Excelente resposta no ajuste de mapa via Power Tuner.",
                "Stage 2: Escape completo + Filtro de espuma + Remap.");
        add("lander_250", "Yamaha XTZ 250 Lander / Ténéré", "Moto de Trilha", "249cc Monocilíndrico Flex",
                "Corrente On/Off", 153,
                21, 22.5, 24, 27, 32, 2.1, 2.25, 2.4, 2.7, 3.1, false,
                "Moto trail focada em durabilidade e economia. Ganho de 1,5 cv no remap não justifica o custo.",
                "Não compensa preparar motor. Invista em pneus cravo e suspensão.");
        add("r1250gs", "BMW R 1250 GS Adventure (Big Trail)", "Moto de Trilha", "1.254cc Boxer ShiftCam", "Cardã", 268,
                136, 146, 154, 168, 185, 14.5, 15.6, 16.5, 17.8, 19.5, true,
                "Rei das Big Trails. O remap suaviza a resposta em baixa rotação no off-road e melhora retomadas carregada.",
                "Stage 2: Coletor sem catalisador + Ponteira Akrapovic + Filtro esportivo + Remap.");
    }

    private void configurarInterface() {
        janela = new JFrame("FREZPREPARAÇÕES SERVER - Controle & Telemetria");
        janela.setSize(920, 620);
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        janela.setLocationRelativeTo(null);
        janela.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        topPanel.setBackground(new Color(15, 23, 42));

        btnIniciar = new JButton("▶ Iniciar Servidor");
        btnIniciar.setBackground(new Color(2, 132, 199));
        btnIniciar.setForeground(Color.WHITE);
        btnIniciar.setFocusPainted(false);

        btnParar = new JButton("■ Parar Servidor");
        btnParar.setBackground(new Color(225, 29, 72));
        btnParar.setForeground(Color.WHITE);
        btnParar.setEnabled(false);
        btnParar.setFocusPainted(false);

        statusLabel = new JLabel("● SERVIDOR PARADO");
        statusLabel.setForeground(new Color(248, 113, 113));
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        topPanel.add(btnIniciar);
        topPanel.add(btnParar);
        topPanel.add(statusLabel);
        janela.add(topPanel, BorderLayout.NORTH);

        JTabbedPane abas = new JTabbedPane();

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(11, 17, 32));
        logArea.setForeground(new Color(56, 189, 248));
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        logArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        abas.addTab("Console & Logs HTTP", new JScrollPane(logArea));
        abas.addTab("Cadastrar Veículo / Moto (Stage 1 a 4)", criarPainelCadastro());

        janela.add(abas, BorderLayout.CENTER);

        btnIniciar.addActionListener(e -> iniciarServidor());
        btnParar.addActionListener(e -> pararServidor());

        janela.setVisible(true);
    }

    private JPanel criarPainelCadastro() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(new EmptyBorder(12, 12, 12, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtNome = new JTextField();
        cbCategoria = new JComboBox<>(new String[] { "Camionete", "Esportivo", "Moto Esportiva", "Moto de Trilha" });
        txtMotor = new JTextField();
        txtTracao = new JTextField("4x4 / Corrente");
        txtPeso = new JTextField("1500");
        txtCvOrig = new JTextField();
        txtCvStg1 = new JTextField();
        txtCvStg2 = new JTextField();
        txtCvStg3 = new JTextField();
        txtCvStg4 = new JTextField();
        txtTqOrig = new JTextField();
        txtTqStg1 = new JTextField();
        txtTqStg2 = new JTextField();
        txtTqStg3 = new JTextField();
        txtTqStg4 = new JTextField();
        chkCompensa = new JCheckBox("Compensa Preparar?", true);
        txtMotivo = new JTextArea(2, 20);
        txtSetup = new JTextArea(2, 20);

        int l = 0;
        adicionarCampo(painel, gbc, l++, "Modelo:", txtNome, "Categoria:", cbCategoria);
        adicionarCampo(painel, gbc, l++, "Motor:", txtMotor, "Tração:", txtTracao);
        adicionarCampo(painel, gbc, l++, "Peso (kg):", txtPeso, "Viabilidade:", chkCompensa);
        adicionarCampo(painel, gbc, l++, "CV Orig / Stg1:", criarDupla(txtCvOrig, txtCvStg1), "CV Stg2 / Stg3 / Stg4:",
                criarTripla(txtCvStg2, txtCvStg3, txtCvStg4));
        adicionarCampo(painel, gbc, l++, "Tq Orig / Stg1:", criarDupla(txtTqOrig, txtTqStg1), "Tq Stg2 / Stg3 / Stg4:",
                criarTripla(txtTqStg2, txtTqStg3, txtTqStg4));

        gbc.gridx = 0;
        gbc.gridy = l;
        gbc.gridwidth = 1;
        painel.add(new JLabel("Análise Técnica:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        painel.add(new JScrollPane(txtMotivo), gbc);
        l++;

        gbc.gridx = 0;
        gbc.gridy = l;
        gbc.gridwidth = 1;
        painel.add(new JLabel("Setup Recomendado:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        painel.add(new JScrollPane(txtSetup), gbc);
        l++;

        JButton btnSalvar = new JButton("+ Adicionar ao Catálogo FrezPreparações");
        btnSalvar.setBackground(new Color(2, 132, 199));
        btnSalvar.setForeground(Color.WHITE);
        gbc.gridx = 1;
        gbc.gridy = l;
        gbc.gridwidth = 2;
        painel.add(btnSalvar, gbc);

        btnSalvar.addActionListener(e -> salvarNovoVeiculo());
        return painel;
    }

    private JPanel criarDupla(JTextField a, JTextField b) {
        JPanel p = new JPanel(new GridLayout(1, 2, 4, 0));
        p.add(a);
        p.add(b);
        return p;
    }

    private JPanel criarTripla(JTextField a, JTextField b, JTextField c) {
        JPanel p = new JPanel(new GridLayout(1, 3, 4, 0));
        p.add(a);
        p.add(b);
        p.add(c);
        return p;
    }

    private void adicionarCampo(JPanel p, GridBagConstraints gbc, int y, String l1, JComponent c1, String l2,
            JComponent c2) {
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.weightx = 0.1;
        p.add(new JLabel(l1), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.4;
        p.add(c1, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0.1;
        p.add(new JLabel(l2), gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.4;
        p.add(c2, gbc);
    }

    private void salvarNovoVeiculo() {
        try {
            String nome = txtNome.getText().trim();
            if (nome.isEmpty())
                throw new Exception("Preencha o nome do veículo.");
            String id = nome.toLowerCase().replaceAll("[^a-z0-9]", "_");

            Veiculo novo = new Veiculo(
                    id, nome, (String) cbCategoria.getSelectedItem(), txtMotor.getText().trim(),
                    txtTracao.getText().trim(), Integer.parseInt(txtPeso.getText().trim()),
                    Integer.parseInt(txtCvOrig.getText().trim()),
                    Integer.parseInt(txtCvStg1.getText().trim()),
                    Integer.parseInt(txtCvStg2.getText().trim()),
                    Integer.parseInt(txtCvStg3.getText().trim()),
                    Integer.parseInt(txtCvStg4.getText().trim()),
                    Double.parseDouble(txtTqOrig.getText().trim()),
                    Double.parseDouble(txtTqStg1.getText().trim()),
                    Double.parseDouble(txtTqStg2.getText().trim()),
                    Double.parseDouble(txtTqStg3.getText().trim()),
                    Double.parseDouble(txtTqStg4.getText().trim()),
                    chkCompensa.isSelected(),
                    txtMotivo.getText().trim(),
                    txtSetup.getText().trim());

            bancoDeVeiculos.put(id, novo);
            log("[CADASTRO] Adicionado: " + nome);
            JOptionPane.showMessageDialog(janela, "Veículo adicionado com sucesso!");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(janela, "Verifique os valores numéricos de Peso, CV e Torque!", "Erro",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(janela, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void log(String msg) {
        if (GraphicsEnvironment.isHeadless() || logArea == null) {
            System.out.println(msg);
        } else {
            SwingUtilities.invokeLater(() -> {
                logArea.append(msg + "\n");
                logArea.setCaretPosition(logArea.getDocument().getLength());
            });
        }
    }

    private void iniciarServidor() {
        try {
            server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/", new StaticFileHandler());
            server.createContext("/api/carros", new ApiListaHandler());
            server.setExecutor(null);
            server.start();

            if (!GraphicsEnvironment.isHeadless()) {
                btnIniciar.setEnabled(false);
                btnParar.setEnabled(true);
                statusLabel.setText("● ONLINE EM http://localhost:8080");
                statusLabel.setForeground(new Color(56, 189, 248));
            }
            log("[FREZPREPARAÇÕES] Servidor iniciado na porta 8080 com " + bancoDeVeiculos.size() + " veículos.");
        } catch (IOException ex) {
            log("[ERRO] Porta 8080 ocupada: " + ex.getMessage());
        }
    }

    private void pararServidor() {
        if (server != null) {
            server.stop(0);
            btnIniciar.setEnabled(true);
            btnParar.setEnabled(false);
            statusLabel.setText("● SERVIDOR PARADO");
            statusLabel.setForeground(new Color(248, 113, 113));
            log("[SISTEMA] Servidor encerrado.");
        }
    }

    class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/"))
                path = "/index.html";

            File file = new File("." + path);
            if (file.exists() && !file.isDirectory() && !path.endsWith(".java") && !path.endsWith(".class")) {
                String contentType = "text/html";
                if (path.endsWith(".css"))
                    contentType = "text/css";
                else if (path.endsWith(".js"))
                    contentType = "application/javascript";

                byte[] bytes = Files.readAllBytes(file.toPath());
                exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } else {
                String erro = "404 Not Found";
                exchange.sendResponseHeaders(404, erro.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(erro.getBytes());
                }
            }
        }
    }

    class ApiListaHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder json = new StringBuilder("[");
            int i = 0;
            for (Veiculo v : bancoDeVeiculos.values()) {
                json.append(v.toJson());
                if (i < bancoDeVeiculos.size() - 1)
                    json.append(",");
                i++;
            }
            json.append("]");

            byte[] bytes = json.toString().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
            log("[API] Frota enviada (" + bancoDeVeiculos.size() + " modelos).");
        }
    }

    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            new ServidorRemap();
        } else {
            SwingUtilities.invokeLater(() -> new ServidorRemap());
        }
    }
}