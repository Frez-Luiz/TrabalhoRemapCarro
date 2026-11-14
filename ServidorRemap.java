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

    // Campos de cadastro na interface Swing
    private JTextField txtNome, txtMotor, txtTracao, txtPeso, txtCvOrig, txtCvStg1, txtCvStg2, txtTqOrig, txtTqStg1,
            txtTqStg2;
    private JComboBox<String> cbCategoria;
    private JCheckBox chkCompensa;
    private JTextArea txtMotivo, txtSetup;

    private static final Map<String, Carro> bancoDeCarros = new LinkedHashMap<>();

    static class Carro {
        String id, nome, categoria, motor, tracao;
        int pesoKg;
        int cvOrig, cvStg1, cvStg2;
        int tqOrig, tqStg1, tqStg2;
        boolean compensa;
        String motivo, recomendacao;

        public Carro(String id, String nome, String categoria, String motor, String tracao, int pesoKg,
                int cvOrig, int cvStg1, int cvStg2,
                int tqOrig, int tqStg1, int tqStg2,
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
            this.tqOrig = tqOrig;
            this.tqStg1 = tqStg1;
            this.tqStg2 = tqStg2;
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
                            "\"cvOrig\":%d,\"cvStg1\":%d,\"cvStg2\":%d," +
                            "\"tqOrig\":%d,\"tqStg1\":%d,\"tqStg2\":%d," +
                            "\"compensa\":%b,\"motivo\":\"%s\",\"recomendacao\":\"%s\"}",
                    escaparJson(id), escaparJson(nome), escaparJson(categoria), escaparJson(motor),
                    escaparJson(tracao), pesoKg,
                    cvOrig, cvStg1, cvStg2, tqOrig, tqStg1, tqStg2,
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
        // --- CAMIONETES ---
        bancoDeCarros.put("hilux", new Carro(
                "hilux", "Toyota Hilux 2.8 Turbo Diesel", "Camionete", "2.8 16V 1GD-FTV Turbo Diesel", "4x4", 2090,
                204, 255, 285, 50, 62, 69, true,
                "Motores turbo diesel possuem grande margem térmica de fábrica. O ganho de +12 kgfm no Stage 1 elimina a lentidão nas retomadas com caçamba carregada.",
                "Stage 2: Downpipe em inox 3\" + Filtro inbox lavável + Remap de motor e câmbio + Solução de DPF/EGR (uso exclusivo off-road)."));

        bancoDeCarros.put("amarok", new Carro(
                "amarok", "VW Amarok V6 3.0 TDI", "Camionete", "3.0 V6 Turbo Diesel", "4Motion Integral", 2180,
                258, 318, 350, 59, 72, 79, true,
                "Referência absoluta em preparação diesel no Brasil. Com tração integral permanente, transfere toda a potência extra para o asfalto sem destracionar.",
                "Stage 1 ou 2 de motor + Calibração de câmbio ZF8 para trocas mais rápidas e aumento de pressão de linha."));

        bancoDeCarros.put("ranger_v6", new Carro(
                "ranger_v6", "Ford Ranger Limited 3.0 V6 Diesel", "Camionete", "3.0 V6 Lion Turbo Diesel", "4WD", 2350,
                250, 305, 335, 61, 73, 79, true,
                "O motor V6 da nova geração Ranger responde de forma linear ao remap, melhorando drasticamente o consumo em rodovia e a resposta em baixa rotação.",
                "Stage 1 de motor e câmbio de 10 marchas + Filtro esportivo de alto fluxo."));

        bancoDeCarros.put("ranger_raptor", new Carro(
                "ranger_raptor", "Ford Ranger Raptor 3.0 V6 Bi-Turbo", "Camionete", "3.0 V6 EcoBoost Bi-Turbo Gasolina",
                "4WD Permanente", 2415,
                397, 460, 505, 59, 69, 76, true,
                "Bloco em ferro grafite compactado (CGI) e sistema anti-lag de fábrica. Aumentar a pressão das duas turbinas transforma a picape em um foguete.",
                "Stage 1 calibrado para gasolina Octapro/Podium + Intercooler frontal maior para manter a temperatura de admissão baixa."));

        bancoDeCarros.put("s10_diesel", new Carro(
                "s10_diesel", "Chevrolet S10 2.8 Turbo Diesel", "Camionete", "2.8 Duramax Turbo Diesel", "4x4", 2050,
                200, 248, 275, 51, 61, 67, true,
                "O motor Duramax possui turbina de geometria variável robusta e bicos injetores com boa vazão original, permitindo ganhos sólidos sem comprometer a durabilidade.",
                "Stage 1 + Remoção do limitador de velocidade final + Calibração do pedal eletrônico."));

        bancoDeCarros.put("ram_2500", new Carro(
                "ram_2500", "RAM 2500 Laramie 6.7 Cummins", "Camionete", "6.7 6 Cilindros em Linha Turbo Diesel", "4x4",
                3440,
                365, 440, 510, 110, 132, 150, true,
                "Motor de caminhão leve com torque colossal. O remap libera a restrição eletrônica imposta para controle de emissões, entregando força bruta para reboque.",
                "Stage 2 + Escapamento direto 4\" em inox + Reforço no conversor de torque do câmbio."));

        bancoDeCarros.put("s10_flex", new Carro(
                "s10_flex", "Chevrolet S10 2.5 Flex (Aspirada)", "Camionete", "2.5 Ecotec Aspirado", "4x2 / 4x4", 1920,
                206, 216, 222, 27, 29, 30, false,
                "Por ser um motor aspirado em uma camionete de quase 2 toneladas, o ganho de apenas 10 cv não é sentido na prática. O custo-benefício é muito baixo.",
                "Não gaste com remap de potência. Instale apenas um módulo de pedal (Piggyback) para melhorar a resposta de saída."));

        // --- CARROS ESPORTIVOS ---
        bancoDeCarros.put("golf_gti", new Carro(
                "golf_gti", "VW Golf GTI MK7 (2.0 TSI)", "Esportivo", "2.0 Turbo (EA888 Gen3)", "Dianteira (FWD)", 1370,
                220, 295, 330, 35, 46, 51, true,
                "O motor EA888 é o rei do custo-benefício em preparação. Apenas com software ganha 75 cv, baixando o 0 a 100 km/h para casa dos 5,4 segundos.",
                "Stage 2: Downpipe 3\" em inox + Intake + Velas grau 8 + Remap de Motor e Câmbio DSG (DQ250)."));

        bancoDeCarros.put("audi_rs3", new Carro(
                "audi_rs3", "Audi RS3 Sedan 2.5 TFSI", "Esportivo", "2.5 5 Cilindros Turbo (DAZA)", "Quattro (AWD)",
                1515,
                400, 485, 540, 49, 62, 68, true,
                "O lendário 5 cilindros da Audi tem internos forjados e responde absurdamente bem a pressão de turbo e etanol (mapa Flex/E85).",
                "Stage 2 E85: Downpipe + Intercooler + Inlet pipe + Remap de motor e câmbio S-Tronic."));

        bancoDeCarros.put("bmw_320i", new Carro(
                "bmw_320i", "BMW 320i G20 (2.0 Turbo)", "Esportivo", "2.0 TwinPower Turbo (B48)", "Traseira (RWD)",
                1460,
                184, 265, 295, 30, 42, 47, true,
                "A 320i usa praticamente o mesmo conjunto mecânico da 330i (258 cv), sendo capada eletronicamente pela BMW. O Stage 1 desbloqueia +80 cv instantaneamente.",
                "Stage 1 via porta OBD2 + Filtro esportivo inbox. Não exige troca de peças físicas."));

        bancoDeCarros.put("bmw_m3", new Carro(
                "bmw_m3", "BMW M3 Competition (G80)", "Esportivo", "3.0 6 Cilindros Bi-Turbo (S58)",
                "Traseira / M xDrive", 1730,
                510, 620, 680, 66, 80, 88, true,
                "O motor S58 é superdimensionado de fábrica. Mesmo no Stage 1 já supera os 600 cv com segurança térmica exemplar.",
                "Stage 2: Downpipes sem catalisador + Midpipe + Remap customizado."));

        bancoDeCarros.put("civic_si", new Carro(
                "civic_si", "Honda Civic Si 2.0 (K20 Aspirado)", "Esportivo", "2.0 i-VTEC Aspirado", "Dianteira (FWD)",
                1320,
                192, 204, 218, 19, 20, 22, false,
                "Motores Honda aspirados de alta rotação (8.000 rpm) já saem de fábrica extraindo quase o máximo possível sem sobrealimentação.",
                "Remap isolado não compensa. Só vale a pena se combinado com coletor 4x2x1, escape 2.5\", intake e acerto via Hondata FlashPro."));

        bancoDeCarros.put("mustang_gt", new Carro(
                "mustang_gt", "Ford Mustang GT 5.0 V8 (Coyote)", "Esportivo", "5.0 V8 Aspirado (Coyote Gen3)",
                "Traseira (RWD)", 1780,
                466, 495, 515, 56, 60, 63, true,
                "Embora seja aspirado, a alta cilindrada (5.0 litros) e a injeção direta/indireta dupla permitem ganhar cerca de 30 a 50 cv reais acertando ponto e mistura.",
                "Stage 2: Cold Air Intake (JLT/PMAS) + Coletor de admissão GT350 + Remap de motor e câmbio automático de 10 marchas."));

        bancoDeCarros.put("polo_gts", new Carro(
                "polo_gts", "VW Polo GTS 1.4 TSI", "Esportivo", "1.4 Turbo (EA211)", "Dianteira (FWD)", 1213,
                150, 185, 205, 25, 32, 35, true,
                "Por pesar apenas 1.213 kg, o ganho de 35 cv e 7 kgfm deixa o hatch muito ágil na cidade e em estradas sinuosas.",
                "Stage 2: Downpipe + Filtro esportivo + Difusor de escape + Remap."));
    }

    private void configurarInterface() {
        janela = new JFrame("APEX REMAP SERVER - Painel de Controle & Telemetria");
        janela.setSize(880, 600);
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        janela.setLocationRelativeTo(null);
        janela.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        topPanel.setBackground(new Color(24, 24, 28));

        btnIniciar = new JButton("▶ Iniciar Servidor");
        btnIniciar.setBackground(new Color(46, 125, 50));
        btnIniciar.setForeground(Color.WHITE);
        btnIniciar.setFocusPainted(false);

        btnParar = new JButton("■ Parar Servidor");
        btnParar.setBackground(new Color(198, 40, 40));
        btnParar.setForeground(Color.WHITE);
        btnParar.setEnabled(false);
        btnParar.setFocusPainted(false);

        statusLabel = new JLabel("● SERVIDOR PARADO");
        statusLabel.setForeground(new Color(239, 83, 80));
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        topPanel.add(btnIniciar);
        topPanel.add(btnParar);
        topPanel.add(statusLabel);
        janela.add(topPanel, BorderLayout.NORTH);

        JTabbedPane abas = new JTabbedPane();

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(18, 18, 20));
        logArea.setForeground(new Color(0, 230, 118));
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        logArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        abas.addTab("Console & Requisições HTTP", new JScrollPane(logArea));
        abas.addTab("Cadastrar Novo Veículo no Sistema", criarPainelCadastro());

        janela.add(abas, BorderLayout.CENTER);

        btnIniciar.addActionListener(e -> iniciarServidor());
        btnParar.addActionListener(e -> pararServidor());

        janela.setVisible(true);
    }

    private JPanel criarPainelCadastro() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtNome = new JTextField();
        cbCategoria = new JComboBox<>(new String[] { "Camionete", "Esportivo" });
        txtMotor = new JTextField();
        txtTracao = new JTextField("4x4 / AWD");
        txtPeso = new JTextField("1500");
        txtCvOrig = new JTextField();
        txtCvStg1 = new JTextField();
        txtCvStg2 = new JTextField();
        txtTqOrig = new JTextField();
        txtTqStg1 = new JTextField();
        txtTqStg2 = new JTextField();
        chkCompensa = new JCheckBox("Compensa fazer Remap?", true);
        txtMotivo = new JTextArea(2, 20);
        txtSetup = new JTextArea(2, 20);

        int linha = 0;
        adicionarCampo(painel, gbc, linha++, "Modelo:", txtNome, "Categoria:", cbCategoria);
        adicionarCampo(painel, gbc, linha++, "Motorização:", txtMotor, "Tração:", txtTracao);
        adicionarCampo(painel, gbc, linha++, "Peso (kg):", txtPeso, "Viabilidade:", chkCompensa);
        adicionarCampo(painel, gbc, linha++, "CV Original:", txtCvOrig, "CV Stage 1:", txtCvStg1);
        adicionarCampo(painel, gbc, linha++, "CV Stage 2:", txtCvStg2, "Torque Orig (kgfm):", txtTqOrig);
        adicionarCampo(painel, gbc, linha++, "Torque Stage 1:", txtTqStg1, "Torque Stage 2:", txtTqStg2);

        gbc.gridx = 0;
        gbc.gridy = linha;
        gbc.gridwidth = 1;
        painel.add(new JLabel("Análise Técnica:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        painel.add(new JScrollPane(txtMotivo), gbc);
        linha++;

        gbc.gridx = 0;
        gbc.gridy = linha;
        gbc.gridwidth = 1;
        painel.add(new JLabel("Setup Recomendado:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        painel.add(new JScrollPane(txtSetup), gbc);
        linha++;

        JButton btnSalvar = new JButton("+ Adicionar Carro ao Site");
        btnSalvar.setBackground(new Color(229, 57, 53));
        btnSalvar.setForeground(Color.WHITE);
        gbc.gridx = 1;
        gbc.gridy = linha;
        gbc.gridwidth = 2;
        painel.add(btnSalvar, gbc);

        btnSalvar.addActionListener(e -> salvarNovoCarro());
        return painel;
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
                    Integer.parseInt(txtTqOrig.getText().trim()),
                    Integer.parseInt(txtTqStg1.getText().trim()),
                    Integer.parseInt(txtTqStg2.getText().trim()),
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
                statusLabel.setForeground(new Color(102, 187, 106));
            }
            log("[SISTEMA] Servidor HTTP Java iniciado na porta 8080.");
            log("[ACESSO] Abra http://localhost:8080 no seu navegador.");
        } catch (IOException ex) {
            log("[ERRO] Não foi possível abrir a porta 8080: " + ex.getMessage());
        }
    }

    private void pararServidor() {
        if (server != null) {
            server.stop(0);
            btnIniciar.setEnabled(true);
            btnParar.setEnabled(false);
            statusLabel.setText("● SERVIDOR PARADO");
            statusLabel.setForeground(new Color(239, 83, 80));
            log("[SISTEMA] Servidor encerrado pelo usuário.");
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
            log("[API] Catálogo enviado (" + bancoDeCarros.size() + " veículos).");
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