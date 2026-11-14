let catalogoCarros = [];
let carroAtual = null;
let stageAtual = 1;
let categoriaAtual = 'Todos';
let tamanhoFonte = 16;

// --- 1. INICIALIZAÇÃO E CARREGAMENTO DO SERVIDOR JAVA ---
window.addEventListener('DOMContentLoaded', async () => {
    carregarPreferencias();
    try {
        const resposta = await fetch('/api/carros');
        catalogoCarros = await resposta.json();
        atualizarListaSelect();
        gerarAtalhosRapidos();
        preencherComparador();
    } catch (erro) {
        console.error('Erro ao conectar com o Java:', erro);
        document.getElementById('status-texto').textContent = 'Servidor Offline';
        document.getElementById('select-carro').innerHTML =
            '<option value="">Erro: Execute o ServidorRemap.java primeiro!</option>';
    }
});

// --- 2. TEMA CLARO / ESCURO & ACESSIBILIDADE DE FONTE ---
function carregarPreferencias() {
    const temaSalvo = localStorage.getItem('apex_tema') || 'dark';
    document.documentElement.setAttribute('data-theme', temaSalvo);
    atualizarBotaoTema(temaSalvo);

    const fonteSalva = parseInt(localStorage.getItem('apex_fonte'));
    if (fonteSalva) {
        tamanhoFonte = fonteSalva;
        document.documentElement.style.setProperty('--font-base', `${tamanhoFonte}px`);
    }
}

document.getElementById('btn-tema').addEventListener('click', () => {
    const atual = document.documentElement.getAttribute('data-theme');
    const novo = atual === 'dark' ? 'light' : 'dark';
    document.documentElement.setAttribute('data-theme', novo);
    localStorage.setItem('apex_tema', novo);
    atualizarBotaoTema(novo);
});

function atualizarBotaoTema(tema) {
    const btn = document.getElementById('btn-tema');
    btn.textContent = tema === 'dark' ? '☀️ Tema Claro' : '🌙 Tema Escuro';
}

document.getElementById('btn-font-inc').addEventListener('click', () => alterarFonte(2));
document.getElementById('btn-font-dec').addEventListener('click', () => alterarFonte(-2));
document.getElementById('btn-font-reset').addEventListener('click', () => {
    tamanhoFonte = 16;
    aplicarFonte();
});

function alterarFonte(delta) {
    tamanhoFonte = Math.min(Math.max(tamanhoFonte + delta, 12), 22);
    aplicarFonte();
}

function aplicarFonte() {
    document.documentElement.style.setProperty('--font-base', `${tamanhoFonte}px`);
    localStorage.setItem('apex_fonte', tamanhoFonte);
}

// --- 3. FILTROS, BUSCA E ATALHOS RÁPIDOS ---
function atualizarListaSelect() {
    const select = document.getElementById('select-carro');
    const termoBusca = document.getElementById('input-busca').value.toLowerCase().trim();

    select.innerHTML = '<option value="">-- Escolha um modelo para simular --</option>';

    const filtrados = catalogoCarros.filter(carro => {
        const passaCategoria =
            categoriaAtual === 'Todos' ||
            (categoriaAtual === 'Compensa' && carro.compensa) ||
            carro.categoria === categoriaAtual;

        const passaBusca =
            carro.nome.toLowerCase().includes(termoBusca) ||
            carro.motor.toLowerCase().includes(termoBusca);

        return passaCategoria && passaBusca;
    });

    filtrados.forEach(carro => {
        const opt = document.createElement('option');
        opt.value = carro.id;
        opt.textContent = `[${carro.categoria}] ${carro.nome} (${carro.cvOrig} cv)`;
        select.appendChild(opt);
    });

    if (filtrados.length === 0) {
        select.innerHTML = '<option value="">Nenhum veículo encontrado para este filtro</option>';
    }
}

document.querySelectorAll('.btn-filtro').forEach(btn => {
    btn.addEventListener('click', () => {
        document.querySelectorAll('.btn-filtro').forEach(b => b.classList.remove('ativo'));
        btn.classList.add('ativo');
        categoriaAtual = btn.dataset.cat;
        atualizarListaSelect();
    });
});

document.getElementById('input-busca').addEventListener('input', atualizarListaSelect);

function gerarAtalhosRapidos() {
    const container = document.getElementById('atalhos-lista');
    const destaques = ['hilux', 'amarok', 'ranger_raptor', 'golf_gti', 'audi_rs3', 'bmw_320i'];

    destaques.forEach(id => {
        const carro = catalogoCarros.find(c => c.id === id);
        if (carro) {
            const chip = document.createElement('button');
            chip.className = 'chip-carro';
            chip.textContent = carro.nome.split('(')[0].trim();
            chip.addEventListener('click', () => {
                document.getElementById('select-carro').value = carro.id;
                carroAtual = carro;
                renderizarTelemetria();
            });
            container.appendChild(chip);
        }
    });
}

// --- 4. TELEMETRIA E DINAMÔMETRO ---
document.getElementById('btn-analisar').addEventListener('click', () => {
    const id = document.getElementById('select-carro').value;
    if (!id) {
        alert('Selecione um veículo primeiro!');
        return;
    }
    carroAtual = catalogoCarros.find(c => c.id === id);
    if (carroAtual) renderizarTelemetria();
});

document.querySelectorAll('.btn-stage').forEach(btn => {
    btn.addEventListener('click', () => {
        document.querySelectorAll('.btn-stage').forEach(b => b.classList.remove('ativo'));
        btn.classList.add('ativo');
        stageAtual = parseInt(btn.dataset.stage);
        if (carroAtual) renderizarTelemetria();
    });
});

function renderizarTelemetria() {
    const painel = document.getElementById('painel-resultado');
    painel.classList.remove('oculto');

    document.getElementById('tag-categoria').textContent = carroAtual.categoria;
    document.getElementById('tag-tracao').textContent = `TRAÇÃO: ${carroAtual.tracao}`;
    document.getElementById('tag-peso').textContent = `${carroAtual.pesoKg} KG`;
    document.getElementById('titulo-carro').textContent = carroAtual.nome;
    document.getElementById('subtitulo-motor').textContent = `Motor: ${carroAtual.motor}`;

    const vereditoEl = document.getElementById('selo-veredito');
    if (carroAtual.compensa) {
        vereditoEl.textContent = '✓ COMPENSA PREPARAR';
        vereditoEl.className = 'veredito sim';
    } else {
        vereditoEl.textContent = '✕ NÃO COMPENSA (BAIXO GANHO)';
        vereditoEl.className = 'veredito nao';
    }

    const cvPrep = stageAtual === 1 ? carroAtual.cvStg1 : carroAtual.cvStg2;
    const tqPrep = stageAtual === 1 ? carroAtual.tqStg1 : carroAtual.tqStg2;

    const ganhoCv = cvPrep - carroAtual.cvOrig;
    const ganhoTq = tqPrep - carroAtual.tqOrig;
    const pctCv = ((ganhoCv / carroAtual.cvOrig) * 100).toFixed(1);
    const pctTq = ((ganhoTq / carroAtual.tqOrig) * 100).toFixed(1);

    // Relação Peso/Potência (quanto menor, mais rápido o carro acelera)
    const pesoPotOrig = (carroAtual.pesoKg / carroAtual.cvOrig).toFixed(2);
    const pesoPotPrep = (carroAtual.pesoKg / cvPrep).toFixed(2);

    document.getElementById('kpi-ganho-cv').textContent = `+${ganhoCv} cv`;
    document.getElementById('kpi-pct-cv').textContent = `+${pctCv}% sobre original`;

    document.getElementById('kpi-ganho-tq').textContent = `+${ganhoTq} kgfm`;
    document.getElementById('kpi-pct-tq').textContent = `+${pctTq}% de força`;

    document.getElementById('kpi-peso-pot').textContent = `${pesoPotPrep} kg/cv`;
    document.getElementById('kpi-peso-orig').textContent = `Original: ${pesoPotOrig} kg/cv`;

    // Barras de Dinamômetro (escala máxima: 700 cv e 155 kgfm para comportar M3 e RAM 2500)
    document.getElementById('lbl-stage-cv').textContent = `Com Remap Stage ${stageAtual}`;
    document.getElementById('lbl-stage-tq').textContent = `Com Remap Stage ${stageAtual}`;

    document.getElementById('val-cv-orig').textContent = `${carroAtual.cvOrig} cv`;
    document.getElementById('val-cv-prep').textContent = `${cvPrep} cv`;
    document.getElementById('badge-cv').textContent = `+${ganhoCv} cv`;

    document.getElementById('val-tq-orig').textContent = `${carroAtual.tqOrig} kgfm`;
    document.getElementById('val-tq-prep').textContent = `${tqPrep} kgfm`;
    document.getElementById('badge-tq').textContent = `+${ganhoTq} kgfm`;

    document.getElementById('bar-cv-orig').style.width = `${Math.min((carroAtual.cvOrig / 700) * 100, 100)}%`;
    document.getElementById('bar-cv-prep').style.width = `${Math.min((cvPrep / 700) * 100, 100)}%`;
    document.getElementById('bar-tq-orig').style.width = `${Math.min((carroAtual.tqOrig / 155) * 100, 100)}%`;
    document.getElementById('bar-tq-prep').style.width = `${Math.min((tqPrep / 155) * 100, 100)}%`;

    document.getElementById('texto-motivo').textContent = carroAtual.motivo;
    document.getElementById('texto-setup').textContent = carroAtual.recomendacao;

    painel.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

// --- 5. COMPARADOR LADO A LADO ---
function preencherComparador() {
    const selA = document.getElementById('comp-carro-a');
    const selB = document.getElementById('comp-carro-b');

    catalogoCarros.forEach(c => {
        selA.add(new Option(c.nome, c.id));
        selB.add(new Option(c.nome, c.id));
    });

    if (catalogoCarros.length > 1) {
        selA.selectedIndex = 0;
        selB.selectedIndex = 1;
    }

    selA.addEventListener('change', atualizarComparador);
    selB.addEventListener('change', atualizarComparador);
}

document.getElementById('btn-abrir-comparador').addEventListener('click', () => {
    const comp = document.getElementById('painel-comparador');
    comp.classList.remove('oculto');
    atualizarComparador();
    comp.scrollIntoView({ behavior: 'smooth' });
});

document.getElementById('btn-fechar-comp').addEventListener('click', () => {
    document.getElementById('painel-comparador').classList.add('oculto');
});

function atualizarComparador() {
    const carroA = catalogoCarros.find(c => c.id === document.getElementById('comp-carro-a').value);
    const carroB = catalogoCarros.find(c => c.id === document.getElementById('comp-carro-b').value);
    if (!carroA || !carroB) return;

    const container = document.getElementById('comp-resultado');
    container.innerHTML = [carroA, carroB].map(c => `
        <div class="comp-col">
            <h4>${c.nome}</h4>
            <div class="comp-linha"><span>Categoria:</span><strong>${c.categoria}</strong></div>
            <div class="comp-linha"><span>Stage 1 Potência:</span><strong>${c.cvStg1} cv (+${c.cvStg1 - c.cvOrig})</strong></div>
            <div class="comp-linha"><span>Stage 1 Torque:</span><strong>${c.tqStg1} kgfm (+${c.tqStg1 - c.tqOrig})</strong></div>
            <div class="comp-linha"><span>Peso / Potência:</span><strong>${(c.pesoKg / c.cvStg1).toFixed(2)} kg/cv</strong></div>
            <div class="comp-linha"><span>Compensa?</span><strong>${c.compensa ? '✓ Sim' : '✕ Não'}</strong></div>
        </div>
    `).join('');
}