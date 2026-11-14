let catalogoCarros = [];
let carroAtual = null;
let stageAtual = 1;
let categoriaAtual = 'Todos';
let tamanhoFonte = 16;

// Informações técnicas de cada Stage (Descrição, Vida Útil base, Custo estimado)
const infoStages = {
    1: {
        texto: 'Stage 1: Apenas reprogramação eletrônica da central (ECU). Mantém todas as peças originais de fábrica.',
        vidaUtilBase: 96,
        custoBase: 2200,
        oleo: 'A cada 8.000 a 10.000 km (sintético original)',
        alerta: 'Componentes originais (turbina, bicos e câmbio) operam dentro da margem de segurança.'
    },
    2: {
        texto: 'Stage 2: Reprogramação + Downpipe em inox + Filtro de ar esportivo (Intake). Melhor fluxo de gases e som esportivo.',
        vidaUtilBase: 88,
        custoBase: 4800,
        oleo: 'A cada 7.000 km (recomendado óleo de alta performance)',
        alerta: 'Atenção à qualidade do combustível e limpeza periódica do filtro esportivo.'
    },
    3: {
        texto: 'Stage 3: Upgrade de Turbina (híbrida ou maior) + Intercooler frontal + Bicos de alta vazão + Remap customizado.',
        vidaUtilBase: 72,
        custoBase: 16500,
        oleo: 'A cada 5.000 km + velas de ignição mais frias (grau 8 ou 9)',
        alerta: 'Exige remap de câmbio/embreagem reforçada para suportar o alto torque sem patinar.'
    },
    4: {
        texto: 'Stage 4: Motor Forjado (pistões, bielas e prisioneiros) + Turbina grande + Reforço completo de transmissão.',
        vidaUtilBase: 52,
        custoBase: 38000,
        oleo: 'A cada 3.000 km ou após uso severo em pista',
        alerta: 'Desgaste acelerado de embreagem, semieixos e pneus. Uso voltado para pista/arrancada.'
    }
};

// --- 1. CARREGAMENTO INICIAL ---
window.addEventListener('DOMContentLoaded', async () => {
    carregarPreferencias();
    try {
        const resposta = await fetch('/api/carros');
        catalogoCarros = await resposta.json();
        atualizarListaSelect();
        gerarAtalhosRapidos();
        preencherComparador();
    } catch (erro) {
        console.error('Erro ao conectar:', erro);
        document.getElementById('status-texto').textContent = 'Servidor Offline';
        document.getElementById('select-carro').innerHTML =
            '<option value="">Erro: Execute o ServidorRemap.java primeiro!</option>';
    }
});

// --- 2. TEMA CLARO/ESCURO & FONTE ---
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
    document.getElementById('btn-tema').textContent = tema === 'dark' ? '☀️ Modo Claro' : '🌙 Modo Escuro';
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

// --- 3. MODAL GUIA DE STAGES ---
document.getElementById('btn-guia').addEventListener('click', () => {
    document.getElementById('modal-guia').classList.remove('oculto');
});
document.getElementById('btn-fechar-guia').addEventListener('click', () => {
    document.getElementById('modal-guia').classList.add('oculto');
});

// --- 4. FILTROS, BUSCA E ATALHOS ---
function atualizarListaSelect() {
    const select = document.getElementById('select-carro');
    const busca = document.getElementById('input-busca').value.toLowerCase().trim();

    select.innerHTML = '<option value="">-- Selecione um modelo na lista --</option>';

    const filtrados = catalogoCarros.filter(c => {
        const okCat =
            categoriaAtual === 'Todos' ||
            (categoriaAtual === 'Compensa' && c.compensa) ||
            c.categoria === categoriaAtual;

        const okBusca =
            c.nome.toLowerCase().includes(busca) ||
            c.motor.toLowerCase().includes(busca);

        return okCat && okBusca;
    });

    filtrados.forEach(c => {
        const opt = document.createElement('option');
        opt.value = c.id;
        opt.textContent = `[${c.categoria}] ${c.nome} — ${c.cvOrig} cv`;
        select.appendChild(opt);
    });

    if (filtrados.length === 0) {
        select.innerHTML = '<option value="">Nenhum veículo encontrado</option>';
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
    const box = document.getElementById('atalhos-lista');
    const ids = ['hilux', 'amarok', 'ranger_raptor', 'golf_gti', 'audi_rs3', 'bmw_320i', 'porsche_911'];

    ids.forEach(id => {
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
            box.appendChild(chip);
        }
    });
}

// --- 5. SIMULAÇÃO, STAGES 1 A 4 E VIDA ÚTIL ---
document.getElementById('btn-analisar').addEventListener('click', () => {
    const id = document.getElementById('select-carro').value;
    if (!id) {
        alert('Escolha um veículo na lista primeiro!');
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

function obterDadosStage(carro, stage) {
    if (stage === 1) return { cv: carro.cvStg1, tq: carro.tqStg1 };
    if (stage === 2) return { cv: carro.cvStg2, tq: carro.tqStg2 };
    if (stage === 3) return { cv: carro.cvStg3, tq: carro.tqStg3 };
    return { cv: carro.cvStg4, tq: carro.tqStg4 };
}

function renderizarTelemetria() {
    const painel = document.getElementById('painel-resultado');
    painel.classList.remove('oculto');

    document.getElementById('tag-categoria').textContent = carroAtual.categoria;
    document.getElementById('tag-tracao').textContent = `TRAÇÃO: ${carroAtual.tracao}`;
    document.getElementById('tag-peso').textContent = `${carroAtual.pesoKg} KG`;
    document.getElementById('titulo-carro').textContent = carroAtual.nome;
    document.getElementById('subtitulo-motor').textContent = `Motorização: ${carroAtual.motor}`;

    const vereditoEl = document.getElementById('selo-veredito');
    if (carroAtual.compensa) {
        vereditoEl.textContent = '✓ COMPENSA PREPARAR';
        vereditoEl.className = 'veredito-pill sim';
    } else {
        vereditoEl.textContent = '✕ BAIXO CUSTO-BENEFÍCIO';
        vereditoEl.className = 'veredito-pill nao';
    }

    const stgInfo = infoStages[stageAtual];
    document.getElementById('desc-stage-atual').textContent = stgInfo.texto;

    const dadosPrep = obterDadosStage(carroAtual, stageAtual);
    const ganhoCv = dadosPrep.cv - carroAtual.cvOrig;
    const ganhoTq = dadosPrep.tq - carroAtual.tqOrig;
    const pctCv = ((ganhoCv / carroAtual.cvOrig) * 100).toFixed(1);
    const pctTq = ((ganhoTq / carroAtual.tqOrig) * 100).toFixed(1);

    // Peso / Potência
    const pesoPotOrig = (carroAtual.pesoKg / carroAtual.cvOrig).toFixed(2);
    const pesoPotPrep = (carroAtual.pesoKg / dadosPrep.cv).toFixed(2);

    // Custo estimado por CV ganho
    const custoPorCv = ganhoCv > 0 ? Math.round(stgInfo.custoBase / ganhoCv) : 0;

    document.getElementById('kpi-cv-final').textContent = `${dadosPrep.cv} cv`;
    document.getElementById('kpi-cv-ganho').textContent = `+${ganhoCv} cv (+${pctCv}%)`;

    document.getElementById('kpi-tq-final').textContent = `${dadosPrep.tq} kgfm`;
    document.getElementById('kpi-tq-ganho').textContent = `+${ganhoTq} kgfm (+${pctTq}%)`;

    document.getElementById('kpi-peso-pot').textContent = `${pesoPotPrep} kg/cv`;
    document.getElementById('kpi-peso-orig').textContent = `Original: ${pesoPotOrig} kg/cv`;

    document.getElementById('kpi-custo-cv').textContent = `R$ ${custoPorCv}/cv`;
    document.getElementById('kpi-investimento').textContent = `Investimento est.: R$ ${stgInfo.custoBase.toLocaleString('pt-BR')}`;

    // Barras de Dinamômetro (escala máxima: 1150 cv e 225 kgfm)
    document.getElementById('lbl-stage-cv').textContent = `Stage ${stageAtual}`;
    document.getElementById('lbl-stage-tq').textContent = `Stage ${stageAtual}`;

    document.getElementById('val-cv-orig').textContent = `${carroAtual.cvOrig} cv`;
    document.getElementById('val-cv-prep').textContent = `${dadosPrep.cv} cv`;
    document.getElementById('badge-cv').textContent = `+${ganhoCv} cv`;

    document.getElementById('val-tq-orig').textContent = `${carroAtual.tqOrig} kgfm`;
    document.getElementById('val-tq-prep').textContent = `${dadosPrep.tq} kgfm`;
    document.getElementById('badge-tq').textContent = `+${ganhoTq} kgfm`;

    document.getElementById('bar-cv-orig').style.width = `${Math.min((carroAtual.cvOrig / 1150) * 100, 100)}%`;
    document.getElementById('bar-cv-prep').style.width = `${Math.min((dadosPrep.cv / 1150) * 100, 100)}%`;
    document.getElementById('bar-tq-orig').style.width = `${Math.min((carroAtual.tqOrig / 225) * 100, 100)}%`;
    document.getElementById('bar-tq-prep').style.width = `${Math.min((dadosPrep.tq / 225) * 100, 100)}%`;

    // Cálculo dinâmico de Vida Útil (penaliza se o carro não compensa ou se o ganho % for extremo)
    let vidaUtil = stgInfo.vidaUtilBase;
    if (!carroAtual.compensa && stageAtual >= 2) vidaUtil -= 12;
    if (parseFloat(pctCv) > 65) vidaUtil -= 5;
    vidaUtil = Math.max(vidaUtil, 35);

    const vuBarra = document.getElementById('vu-barra');
    const vuNota = document.getElementById('vu-nota');
    vuBarra.style.width = `${vidaUtil}%`;
    vuNota.textContent = `${vidaUtil}% DE VIDA ÚTIL ESTIMADA`;

    if (vidaUtil >= 85) {
        vuBarra.style.backgroundColor = 'var(--green)';
        vuNota.style.backgroundColor = 'var(--green-soft)';
        vuNota.style.color = 'var(--green)';
    } else if (vidaUtil >= 65) {
        vuBarra.style.backgroundColor = 'var(--yellow)';
        vuNota.style.backgroundColor = 'rgba(251, 191, 36, 0.15)';
        vuNota.style.color = 'var(--yellow)';
    } else {
        vuBarra.style.backgroundColor = 'var(--red)';
        vuNota.style.backgroundColor = 'var(--red-soft)';
        vuNota.style.color = 'var(--red)';
    }

    document.getElementById('vu-oleo').textContent = stgInfo.oleo;
    document.getElementById('vu-alerta').textContent = stgInfo.alerta;
    document.getElementById('vu-combustivel').textContent =
        carroAtual.motor.toLowerCase().includes('diesel')
            ? 'Diesel S10 aditivado de procedência'
            : (stageAtual >= 2 ? 'Gasolina Podium/Octapro ou Etanol' : 'Gasolina Aditivada / Etanol');

    document.getElementById('texto-motivo').textContent = carroAtual.motivo;
    document.getElementById('texto-setup').textContent = carroAtual.recomendacao;

    painel.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

// --- 6. COMPARADOR LADO A LADO ---
function preencherComparador() {
    const selA = document.getElementById('comp-carro-a');
    const selB = document.getElementById('comp-carro-b');

    catalogoCarros.forEach(c => {
        selA.add(new Option(c.nome, c.id));
        selB.add(new Option(c.nome, c.id));
    });

    if (catalogoCarros.length > 1) {
        selA.selectedIndex = 0;
        selB.selectedIndex = 10; // Compara uma camionete com um esportivo por padrão
    }

    selA.addEventListener('change', atualizarComparador);
    selB.addEventListener('change', atualizarComparador);
    document.getElementById('comp-stage-select').addEventListener('change', atualizarComparador);
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
    const stg = parseInt(document.getElementById('comp-stage-select').value);
    if (!carroA || !carroB) return;

    const container = document.getElementById('comp-resultado');
    container.innerHTML = [carroA, carroB].map(c => {
        const dados = obterDadosStage(c, stg);
        const vu = infoStages[stg].vidaUtilBase - (!c.compensa && stg >= 2 ? 12 : 0);
        return `
            <div class="comp-col">
                <h4>${c.nome}</h4>
                <div class="comp-row"><span>Categoria:</span><strong>${c.categoria} (${c.tracao})</strong></div>
                <div class="comp-row"><span>Potência Stage ${stg}:</span><strong>${dados.cv} cv (+${dados.cv - c.cvOrig})</strong></div>
                <div class="comp-row"><span>Torque Stage ${stg}:</span><strong>${dados.tq} kgfm (+${dados.tq - c.tqOrig})</strong></div>
                <div class="comp-row"><span>Peso / Potência:</span><strong>${(c.pesoKg / dados.cv).toFixed(2)} kg/cv</strong></div>
                <div class="comp-row"><span>Vida Útil Est.:</span><strong>${vu}%</strong></div>
                <div class="comp-row"><span>Compensa?</span><strong>${c.compensa ? '✓ Sim' : '✕ Não'}</strong></div>
            </div>
        `;
    }).join('');
}