// BANCO DE DADOS AUTOMOTIVO
const veiculos = [
    { id: 'hilux', nome: 'Toyota Hilux 2.8 Turbo Diesel', categoria: 'CAMIONETE', cv: 204, kgfm: 51.0, turbo: true },
    { id: 'ranger', nome: 'Ford Ranger 3.2 Turbo Diesel', categoria: 'CAMIONETE', cv: 200, kgfm: 47.9, turbo: true },
    { id: 's10', nome: 'Chevrolet S10 2.8 Turbo Diesel', categoria: 'CAMIONETE', cv: 200, kgfm: 51.0, turbo: true },
    { id: 'amarok', nome: 'VW Amarok 3.0 V6 TDI', categoria: 'CAMIONETE', cv: 258, kgfm: 59.1, turbo: true },
    { id: 'ram1500', nome: 'RAM 1500 5.7 V8 HEMI', categoria: 'CAMIONETE', cv: 400, kgfm: 56.7, turbo: false },
    { id: 'golf_gti', nome: 'VW Golf GTI 2.0 TSI', categoria: 'ESPORTIVO', cv: 230, kgfm: 35.7, turbo: true },
    { id: 'civic_si', nome: 'Honda Civic Si 2.4 Aspirado', categoria: 'ESPORTIVO', cv: 206, kgfm: 23.9, turbo: false },
    { id: 'sandero_rs', nome: 'Renault Sandero R.S. 2.0', categoria: 'ESPORTIVO', cv: 150, kgfm: 20.9, turbo: false },
    { id: 'bmw_330i', nome: 'BMW 330i M Sport 2.0 Turbo', categoria: 'ESPORTIVO', cv: 258, kgfm: 40.8, turbo: true },
    { id: 'mustang', nome: 'Ford Mustang GT 5.0 V8', categoria: 'ESPORTIVO', cv: 483, kgfm: 56.7, turbo: false }
];

// -------------------------------------------------------------
// FUNCIONALIDADES DE ACESSIBILIDADE (FONTE E TEMA)
// -------------------------------------------------------------
let currentFontSize = 16;

document.getElementById('btnFontUp').addEventListener('click', () => {
    if (currentFontSize < 22) {
        currentFontSize += 2;
        document.documentElement.style.setProperty('--font-base', `${currentFontSize}px`);
    }
});

document.getElementById('btnFontDown').addEventListener('click', () => {
    if (currentFontSize > 12) {
        currentFontSize -= 2;
        document.documentElement.style.setProperty('--font-base', `${currentFontSize}px`);
    }
});

document.getElementById('btnFontReset').addEventListener('click', () => {
    currentFontSize = 16;
    document.documentElement.style.setProperty('--font-base', '16px');
});

// ALTERNAR TEMA (CLARO / ESCURO)
const themeBtn = document.getElementById('btnThemeToggle');
const themeIcon = document.getElementById('themeIcon');
const themeLabel = document.getElementById('themeLabel');

themeBtn.addEventListener('click', () => {
    const currentTheme = document.documentElement.getAttribute('data-theme');
    const newTheme = currentTheme === 'dark' ? 'light' : 'dark';

    document.documentElement.setAttribute('data-theme', newTheme);
    themeIcon.className = newTheme === 'dark' ? 'fa-solid fa-moon' : 'fa-solid fa-sun';
    themeLabel.textContent = newTheme === 'dark' ? 'Modo Escuro' : 'Modo Claro';
});

// -------------------------------------------------------------
// SELEÇÃO DINÂMICA DE VEÍCULOS
// -------------------------------------------------------------
document.getElementById('categoria').addEventListener('change', function (e) {
    const cat = e.target.value;
    const selectVeiculo = document.getElementById('veiculo');

    selectVeiculo.innerHTML = '<option value="">-- Selecione o Modelo --</option>';

    if (!cat) {
        selectVeiculo.disabled = true;
        return;
    }

    const filtrados = veiculos.filter(v => v.categoria === cat);
    filtrados.forEach(v => {
        const option = document.createElement('option');
        option.value = v.id;
        option.textContent = v.nome;
        selectVeiculo.appendChild(option);
    });

    selectVeiculo.disabled = false;
});

// -------------------------------------------------------------
// LÓGICA DE SIMULAÇÃO DE REMAP E DESEMPENHO
// -------------------------------------------------------------
document.getElementById('remapForm').addEventListener('submit', function (e) {
    e.preventDefault();

    const idVeiculo = document.getElementById('veiculo').value;
    const estagio = document.getElementById('estagio').value;
    const carro = veiculos.find(v => v.id === idVeiculo);

    if (!carro) return;

    let pctGanho = 0;
    let custo = 0;
    let viabilidade = '';
    let recomendacao = '';
    let aviso = '';

    if (!carro.turbo) {
        if (estagio === 'STAGE1') {
            pctGanho = 0.08;
            custo = 1300;
            viabilidade = 'BAIXA';
            recomendacao = 'Motores aspirados possuem baixo ganho de potência apenas com acerto de software.';
            aviso = 'Custo-benefício baixo se o foco principal for ganho expressivo de cavalaria.';
        } else if (estagio === 'STAGE2') {
            pctGanho = 0.13;
            custo = 3800;
            viabilidade = 'BAIXA';
            recomendacao = 'Ganha resposta e ronco esportivo com escapamento/downpipe, mas aumento modesto de CV.';
            aviso = 'Exige modificação física na linha de escapamento e admissão.';
        } else {
            pctGanho = 0.40;
            custo = 18000;
            viabilidade = 'MEDIA';
            recomendacao = 'Para Stage 3 em motores aspirados é necessária a instalação de Kit Turbo ou Supercharger.';
            aviso = 'Alto custo. Exige substituição e reforço de componentes internos do motor.';
        }
    } else {
        if (estagio === 'STAGE1') {
            pctGanho = 0.25;
            custo = 2300;
            viabilidade = 'ALTA';
            recomendacao = 'Excelente viabilidade! Motores turbo originais respondem muito bem à reprogramação via software.';
            aviso = 'Recomendado manter a manutenção preventiva rigorosa (óleo, velas e bobinas).';
        } else if (estagio === 'STAGE2') {
            pctGanho = 0.35;
            custo = 5800;
            viabilidade = 'ALTA';
            recomendacao = 'Excelente ganho de torque e potência combinando software, filtro esportivo e Downpipe.';
            aviso = 'Atenção às emissões de gases e regulamentações locais.';
        } else {
            pctGanho = 0.60;
            custo = 22000;
            viabilidade = 'MEDIA';
            recomendacao = 'Ganho extremo de potência. Exige substituição do turbocompressor original e mapa customizado.';
            aviso = 'Pode exigir upgrade de embreagem/câmbio e combustível de alta octanagem.';
        }
    }

    const ganhoCv = Math.round(carro.cv * pctGanho);
    const ganhoKgfm = (carro.kgfm * pctGanho).toFixed(1);

    const badge = document.getElementById('viabilidadeBadge');
    badge.textContent = `VIABILIDADE: ${viabilidade}`;
    badge.className = `status-badge ${viabilidade.toLowerCase()}`;

    document.getElementById('recomendacaoTexto').textContent = recomendacao;
    document.getElementById('cvOrig').textContent = `${carro.cv} CV`;
    document.getElementById('ganhoCv').textContent = `+${ganhoCv} CV`;
    document.getElementById('kgfmOrig').textContent = `${carro.kgfm} Kgfm`;
    document.getElementById('ganhoKgfm').textContent = `+${ganhoKgfm} Kgfm`;
    document.getElementById('custoEstimado').textContent = `R$ ${custo.toLocaleString('pt-BR')},00`;
    document.getElementById('alertaMecanico').textContent = aviso;

    document.getElementById('resultCard').style.display = 'block';
});