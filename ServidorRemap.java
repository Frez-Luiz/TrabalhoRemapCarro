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

    private static final Map<String, Carro> bancoDeCarros = new LinkedHashMap<>();

    static class Carro {
        String id, nome, categoria, motor, tracao;
        int pesoKg;
        int cvOrig, cvStg1, cvStg2, cvStg3, cvStg4;
        int tqOrig, tqStg1, tqStg2, tqStg3, tqStg4;
        boolean compensa;
        String motivo, recomendacao;

        public Carro(String id, String nome, String categoria, String motor, String tracao, int pesoKg,
                int cvOrig, int cvStg1, int cvStg2, int cvStg3, int cvStg4,
                int tqOrig, int tqStg1, int tqStg2, int tqStg3, int tqStg4,
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
            return String.format(
                    "{\"id\":\"%s\",\"nome\":\"%s\",\"categoria\":\"%s\",\"motor\":\"%s\"," +
                            "\"tracao\":\"%s\",\"pesoKg\":%d," +
                            "\"cvOrig\":%d,\"cvStg1\":%d,\"cvStg2\":%d,\"cvStg3\":%d,\"cvStg4\":%d," +
                            "\"tqOrig\":%d,\"tqStg1\":%d,\"tqStg2\":%d,\"tqStg3\":%d,\"tqStg4\":%d," +
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
            System.out.println("[AVISO] Ambiente sem monitor detectado (Headless). Iniciando no terminal...");
            iniciarServidor();
        } else {
            configurarInterface();
        }
    }

    private void inicializarDados() {
        // --- CAMIONETES (10 MODELOS) ---
        bancoDeCarros.put("hilux", new Carro(
                "hilux", "Toyota Hilux 2.8 Turbo Diesel", "Camionete", "2.8 16V 1GD-FTV Turbo Diesel", "4x4", 2090,
                204, 255, 285, 340, 410,
                50, 62, 69, 80, 95, true,
                "Motor diesel de baixa rotação e bloco robusto. No Stage 1 e 2 ganha muito torque com total segurança. Stage 3+ exige turbina maior e bicos de alta vazão.",
                "Uso diário/viagem: Stage 2 (Downpipe + Filtro + Remap). Uso extremo: Stage 3 com turbina híbrida."));

        bancoDeCarros.put("amarok", new Carro(
                "amarok", "VW Amarok V6 3.0 TDI", "Camionete", "3.0 V6 Turbo Diesel", "4Motion Integral", 2180,
                258, 318, 355, 420, 490,
                59, 72, 80, 92, 105, true,
                "Referência absoluta entre as picapes médias. A tração integral permanente permite aproveitar cada cavalo sem perder aderência no asfalto.",
                "Stage 2 + Remap de câmbio ZF8. Para Stage 3/4, é obrigatório intercooler frontal maior e upgrade de bomba de alta."));

        bancoDeCarros.put("ranger_v6", new Carro(
                "ranger_v6", "Ford Ranger Limited 3.0 V6 Diesel", "Camionete", "3.0 V6 Lion Turbo Diesel", "4WD", 2350,
                250, 305, 340, 400, 460,
                61, 73, 81, 92, 104, true,
                "O motor V6 da nova geração Ranger possui excelente curva de torque. O remap elimina o atraso do acelerador e melhora o consumo em rodovia.",
                "Stage 1 ou 2 com calibração do câmbio automático de 10 marchas."));

        bancoDeCarros.put("ranger_raptor", new Carro(
                "ranger_raptor", "Ford Ranger Raptor 3.0 V6 Bi-Turbo", "Camionete", "3.0 V6 EcoBoost Bi-Turbo Gasolina",
                "4WD Permanente", 2415,
                397, 460, 510, 590, 680,
                59, 69, 77, 88, 98, true,
                "Bloco em ferro grafite compactado (CGI) e duas turbinas. Responde instantaneamente ao aumento de pressão, entregando desempenho de esportivo.",
                "Stage 2 com gasolina de alta octanagem (Podium) e intercooler de alumínio."));

        bancoDeCarros.put("s10_diesel", new Carro(
                "s10_diesel", "Chevrolet S10 2.8 Turbo Diesel", "Camionete", "2.8 Duramax Turbo Diesel", "4x4", 2050,
                200, 248, 278, 330, 390,
                51, 61, 68, 78, 90, true,
                "O motor Duramax tem excelente reserva mecânica de fábrica, suportando Stage 1 e 2 sem comprometer a durabilidade para trabalho ou estrada.",
                "Stage 2: Downpipe inox + Filtro esportivo + Remap."));

        bancoDeCarros.put("ram_2500", new Carro(
                "ram_2500", "RAM 2500 Laramie 6.7 Cummins", "Camionete", "6.7 6 Cilindros Turbo Diesel", "4x4", 3440,
                365, 445, 520, 650, 800,
                110, 134, 155, 185, 220, true,
                "Motor projetado para serviço pesado. O remap libera a limitação eletrônica original e transforma o torque em força bruta para reboque.",
                "Stage 2 com escapamento 4\" e reforço no conversor de torque da transmissão."));

        bancoDeCarros.put("ram_1500_trx", new Carro(
                "ram_1500_trx", "RAM 1500 TRX 6.2 V8 Supercharged", "Camionete", "6.2 V8 HEMI Hellcat Supercharger",
                "4WD Integral", 2900,
                712, 780, 840, 950, 1100,
                89, 98, 106, 118, 135, true,
                "Equipada com o lendário motor Hellcat com compressor mecânico. Trocar apenas a polia do supercharger e recalibrar já passa dos 800 cv.",
                "Stage 2: Polia menor do compressor + Bicos injetores maiores + Intake + Remap."));

        bancoDeCarros.put("l200_triton", new Carro(
                "l200_triton", "Mitsubishi L200 Triton 2.4 Diesel", "Camionete",
                "2.4 MIVEC Turbo Diesel (Bloco Alumínio)", "Super Select 4x4", 1950,
                190, 232, 258, 300, 350,
                44, 53, 59, 68, 78, true,
                "Por ter bloco em alumínio e ser mais leve que as rivais, o ganho de +42 cv no Stage 1 deixa a picape muito ágil.",
                "Stage 1 ou 2 conservador para preservar a temperatura do bloco de alumínio."));

        bancoDeCarros.put("toro_diesel", new Carro(
                "toro_diesel", "Fiat Toro / Jeep Compass 2.0 TD350", "Camionete", "2.0 Multijet II Turbo Diesel", "4x4",
                1870,
                170, 210, 232, 270, 315,
                35, 44, 49, 56, 64, true,
                "Picape monobloco leve. O ganho de +9 kgfm de torque elimina completamente a sensação de peso nas arrancadas e subidas.",
                "Stage 2: Downpipe + Filtro inbox + Remap."));

        bancoDeCarros.put("s10_flex", new Carro(
                "s10_flex", "Chevrolet S10 2.5 Flex (Aspirada)", "Camionete", "2.5 Ecotec Aspirado", "4x2 / 4x4", 1920,
                206, 216, 224, 245, 290,
                27, 29, 31, 35, 42, false,
                "Por ser aspirada e pesar quase 2 toneladas, Stage 1 e 2 trazem ganhos mínimos. Chegar em Stage 3/4 exige instalar kit turbo do zero (custo altíssimo).",
                "Não compensa preparar. Instale apenas um módulo de pedal eletrônico."));

        // --- CARROS ESPORTIVOS (10 MODELOS) ---
        bancoDeCarros.put("golf_gti", new Carro(
                "golf_gti", "VW Golf GTI MK7 (2.0 TSI)", "Esportivo", "2.0 Turbo (EA888 Gen3)", "Dianteira (FWD)", 1370,
                220, 295, 330, 410, 520,
                35, 46, 51, 58, 68, true,
                "O motor EA888 é o rei do custo-benefício. Stage 1 e 2 mantêm confiabilidade original; Stage 3 usa turbina IS38 (do Golf R); Stage 4 exige miolo forjado.",
                "Stage 2 (Downpipe + Intake + Remap Motor/DSG) é o ponto ideal entre custo, potência e durabilidade."));

        bancoDeCarros.put("audi_rs3", new Carro(
                "audi_rs3", "Audi RS3 Sedan 2.5 TFSI", "Esportivo", "2.5 5 Cilindros Turbo (DAZA)", "Quattro (AWD)",
                1515,
                400, 485, 540, 680, 850,
                49, 62, 68, 82, 96, true,
                "O 5 cilindros da Audi possui internos extremamente resistentes. Com etanol (E85) e turbina híbrida (Stage 3) supera superesportivos.",
                "Stage 2 E85 para rua ou Stage 3 com turbina TTE700 e reforço de embreagem."));

        bancoDeCarros.put("bmw_320i", new Carro(
                "bmw_320i", "BMW 320i G20 (2.0 Turbo)", "Esportivo", "2.0 TwinPower Turbo (B48)", "Traseira (RWD)",
                1460,
                184, 265, 295, 360, 440,
                30, 42, 47, 55, 64, true,
                "A 320i sai limitada eletronicamente de fábrica. O Stage 1 desbloqueia +81 cv sem trocar nenhuma peça.",
                "Stage 1 ou 2 via OBD2. Para Stage 3+, recomenda-se pistões forjados devido à taxa de compressão elevada."));

        bancoDeCarros.put("bmw_m3", new Carro(
                "bmw_m3", "BMW M3 Competition (G80)", "Esportivo", "3.0 6 Cilindros Bi-Turbo (S58)", "M xDrive (AWD)",
                1730,
                510, 620, 680, 820, 1020,
                66, 80, 88, 102, 120, true,
                "O motor S58 tem arquitetura fechada (closed-deck) e componentes forjados de série. Suporta mais de 800 cv sem abrir o bloco.",
                "Stage 2: Downpipes + Midpipe + Remap customizado."));

        bancoDeCarros.put("porsche_911", new Carro(
                "porsche_911", "Porsche 911 Carrera S (992)", "Esportivo", "3.0 6 Cilindros Boxer Bi-Turbo",
                "Traseira (RWD)", 1515,
                450, 535, 575, 660, 780,
                54, 65, 71, 80, 92, true,
                "O conjunto boxer biturbo e o câmbio PDK da Porsche possuem engenharia de pista, entregando ganhos expressivos e repetibilidade sem superaquecer.",
                "Stage 2 com catalisadores esportivos de alto fluxo e acerto fino de ECU."));

        bancoDeCarros.put("civic_type_r", new Carro(
                "civic_type_r", "Honda Civic Type R (FL5)", "Esportivo", "2.0 VTEC Turbo (K20C1)", "Dianteira (FWD)",
                1430,
                297, 355, 385, 450, 540,
                43, 51, 56, 63, 72, true,
                "Diferente dos antigos Civic aspirados, o Type R moderno é turbo e responde muito bem a downpipe, intercooler e calibração.",
                "Stage 2: Downpipe + Intercooler frontal + Intake + Remap."));

        bancoDeCarros.put("civic_si", new Carro(
                "civic_si", "Honda Civic Si 2.0 (K20 Aspirado)", "Esportivo", "2.0 i-VTEC Aspirado", "Dianteira (FWD)",
                1320,
                192, 204, 218, 250, 340,
                19, 20, 22, 25, 36, false,
                "Aspirado de alta rotação: Stage 1 e 2 ganham pouquíssimos cavalos. Stage 3 exige comandos bravos e Stage 4 exige instalação de kit turbo.",
                "Remap puro não compensa. Só faça se for montar projeto completo aspirado ou turbo."));

        bancoDeCarros.put("mustang_gt", new Carro(
                "mustang_gt", "Ford Mustang GT 5.0 V8 (Coyote)", "Esportivo", "5.0 V8 Aspirado (Coyote Gen3)",
                "Traseira (RWD)", 1780,
                466, 495, 520, 680, 850,
                56, 60, 64, 78, 94, true,
                "Por ser um V8 de 5.0 litros, responde bem no Stage 2 com admissão e coletor. No Stage 3/4 recebe kit Supercharger (Whipple/Roush).",
                "Stage 2 Aspirado (Cold Air Intake + Coletor GT350 + Remap)."));

        bancoDeCarros.put("polo_gts", new Carro(
                "polo_gts", "VW Polo GTS / Jetta 1.4 TSI", "Esportivo", "1.4 Turbo (EA211)", "Dianteira (FWD)", 1213,
                150, 185, 205, 245, 300,
                25, 32, 35, 40, 47, true,
                "Excelente relação custo-benefício em carros leves. No Stage 1 e 2 fica muito esperto sem forçar o câmbio automático Aisin.",
                "Stage 2: Downpipe + Filtro inbox + Remap. Evite Stage 3/4 sem reforço de câmbio e pistões."));

        bancoDeCarros.put("lancer_evo", new Carro(
                "lancer_evo", "Mitsubishi Lancer Evolution X", "Esportivo", "2.0 Turbo (4B11T)", "S-AWC (AWD)", 1560,
                295, 345, 380, 460, 580,
                37, 45, 50, 58, 70, true,
                "Plataforma de rali com tração integral avançada. O bloco 4B11T em alumínio suporta bem até Stage 2/3; acima disso exige camisas reforçadas.",
                "Stage 2: Escapamento completo 3\" + Intake + Solenoide de boost + Remap."));
    }

    private void configurarInterface() {
        janela = new JFrame("APEX REMAP SERVER - Painel de Controle & Telemetria");
        janela.setSize(900, 620);
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
        abas.addTab("Console & Requisições HTTP", new JScrollPane(logArea));
        abas.addTab("Cadastrar Novo Veículo (Stage 1 a 4)", criarPainelCadastro());

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
        cbCategoria = new JComboBox<>(new String[] { "Camionete", "Esportivo" });
        txtMotor = new JTextField();
        txtTracao = new JTextField("4x4 / AWD");
        txtPeso = new JTextField("1600");
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
        chkCompensa = new JCheckBox("Compensa fazer Remap?", true);
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

        JButton btnSalvar = new JButton("+ Adicionar Carro ao Site");
        btnSalvar.setBackground(new Color(2, 132, 199));
        btnSalvar.setForeground(Color.WHITE);
        gbc.gridx = 1;
        gbc.gridy = l;
        gbc.gridwidth = 2;
        painel.add(btnSalvar, gbc);

        btnSalvar.addActionListener(e -> salvarNovoCarro());
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

    private void salvarNovoCarro() {
        try {
            String nome = txtNome.getText().trim();
            if (nome.isEmpty())
                throw new Exception("Preencha o nome do veículo.");
            String id = nome.toLowerCase().replaceAll("[^a-z0-9]", "_");

            Carro novo = new Carro(
                    id, nome, (String) cbCategoria.getSelectedItem(), txtMotor.getText().trim(),
                    txtTracao.getText().trim(), Integer.parseInt(txtPeso.getText().trim()),
                    Integer.parseInt(txtCvOrig.getText().trim()),
                    Integer.parseInt(txtCvStg1.getText().trim()),
                    Integer.parseInt(txtCvStg2.getText().trim()),
                    Integer.parseInt(txtCvStg3.getText().trim()),
                    Integer.parseInt(txtCvStg4.getText().trim()),
                    Integer.parseInt(txtTqOrig.getText().trim()),
                    Integer.parseInt(txtTqStg1.getText().trim()),
                    Integer.parseInt(txtTqStg2.getText().trim()),
                    Integer.parseInt(txtTqStg3.getText().trim()),
                    Integer.parseInt(txtTqStg4.getText().trim()),
                    chkCompensa.isSelected(),
                    txtMotivo.getText().trim(),
                    txtSetup.getText().trim());

            bancoDeCarros.put(id, novo);
            log("[CADASTRO] Novo veículo adicionado: " + nome);
            JOptionPane.showMessageDialog(janela, "Veículo cadastrado com sucesso!");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(janela, "Peso, CV e Torque devem ser números inteiros!", "Erro",
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
            server.createContext("/api/carros", new ApiListaCarrosHandler());
            server.setExecutor(null);
            server.start();

            if (!GraphicsEnvironment.isHeadless()) {
                btnIniciar.setEnabled(false);
                btnParar.setEnabled(true);
                statusLabel.setText("● ONLINE EM http://localhost:8080");
                statusLabel.setForeground(new Color(56, 189, 248));
            }
            log("[SISTEMA] Servidor HTTP Java iniciado na porta 8080.");
            log("[ACESSO] Abra http://localhost:8080 no navegador.");
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
                log("[GET] 200 OK -> " + path);
            } else {
                String erro = "404 Not Found";
                exchange.sendResponseHeaders(404, erro.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(erro.getBytes());
                }
            }
        }
    }

    class ApiListaCarrosHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder json = new StringBuilder("[");
            int i = 0;
            for (Carro c : bancoDeCarros.values()) {
                json.append(c.toJson());
                if (i < bancoDeCarros.size() - 1)
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
            log("[API] Frota completa enviada (" + bancoDeCarros.size() + " veículos).");
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