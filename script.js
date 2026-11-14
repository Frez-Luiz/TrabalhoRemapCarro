let frotaCompleta = [];
let veiculoAtual = null;
let stageAtual = 1;
let categoriaAtual = 'Todos';
let tamanhoFonte = 16;

const dadosStages = {
    1: {
        texto: 'Stage 1: Calibração eletrônica da central (ECU). Mantém escapamento, filtro e motor 100% originais.',
        vidaUtilBase: 96,
        custoBase: 2000,
        oleo: 'Troca no prazo original ou a cada 8.000 km (motos: 3.000 km)',
        alerta: 'Conjunto mecânico original trabalha dentro da margem de segurança de fábrica.'
    },
    2: {
        texto: 'Stage 2: Remap + Escapamento/Downpipe esportivo + Filtro de ar de alto fluxo. Melhora resposta e reduz temperatura.',
        vidaUtilBase: 89,
        custoBase: 4500,
        oleo: 'Reduzir intervalo de troca em 20% com óleo 100% sintético',
        alerta: 'Mantenha o filtro de ar sempre limpo e utilize combustível de boa procedência.'
    },
    3: {
        texto: 'Stage 3: Upgrade de Turbina/Compressor (carros/camionetes) ou Comandos/Bicos/Central racing (motos).',
        vidaUtilBase: 73,
        custoBase: 15500,
        oleo: 'A cada 5.000 km (motos: a cada 15h de uso ou 1.500 km)',
        alerta: 'Exige reforço de embreagem/câmbio e sistema de arrefecimento revisado.'
    },
    4: {
        texto: 'Stage 4: Preparação extrema com internos forjados (pistões/bielas) e acerto voltado para pista e competição.',
        vidaUtilBase: 54,
        custoBase: 35000,
        oleo: 'A cada 3.000 km ou após cada evento de pista/trilha',
        alerta: 'Manutenção preventiva rigorosa; desgaste acelerado de transmissão e pneus.'
    }
};

// --- 1. INICIALIZAÇÃO ---
window.addEventListener('DOMContentLoaded', async () => {
    carregarPreferencias();
    try {
        const res = await fetch('/api/carros');
        frotaCompleta = await res.json();
        atualizarSelect();
        gerarAtalhos();
        preencherComparador();
    } catch (erro) {
        console.error(erro);
        document.getElementById('status-texto').textContent = 'Servidor Offline';
        document.getElementById('select-veiculo').innerHTML =
            '<option value="">Erro: Inicie o ServidorRemap.java primeiro!</option>';
    }
});

// --- 2. TEMA CLARO / ESCURO & FONTE ---
function carregarPreferencias() {
    const tema = localStorage.getItem('frez_tema') || 'dark';
    document.documentElement.setAttribute('data-theme', tema);
    document.getElementById('btn-tema').textContent = tema === 'dark' ? '☀️ Tema Claro' : '🌙 Tema Escuro';

    const fonte = parseInt(localStorage.getItem('frez_fonte'));
    if (fonte) {
        tamanhoFonte = fonte;
        document.documentElement.style.setProperty('--font-base', `${tamanhoFonte}px`);
    }
}

document.getElementById('btn-tema').addEventListener('click', () => {
    const atual = document.documentElement.getAttribute('data-theme');
    const novo = atual === 'dark' ? 'light' : 'dark';
    document.documentElement.setAttribute('data-theme', novo);
    localStorage.setItem('frez_tema', novo);
    document.getElementById('btn-tema').textContent = novo === 'dark' ? '☀️ Tema Claro' : '🌙 Tema Escuro';
});

document.getElementById('btn-font-inc').addEventListener('click', () => mudarFonte(2));
document.getElementById('btn-font-dec').addEventListener('click', () => mudarFonte(-2));
document.getElementById('btn-font-reset').addEventListener('click', () => {
    tamanhoFonte = 16;
    aplicarFonte();
});

function mudarFonte(delta) {
    tamanhoFonte = Math.min(Math.max(tamanhoFonte + delta, 12), 22);
    aplicarFonte();
}

function aplicarFonte() {
    document.documentElement.style.setProperty('--font-base', `${tamanhoFonte}px`);
    localStorage.setItem('frez_fonte', tamanhoFonte);
}

// --- 3. MODAL GUIA ---
document.getElementById('btn-guia').addEventListener('click', () => {
    document.getElementById('modal-guia').classList.remove('oculto');
});
document.getElementById('btn-fechar-guia').addEventListener('click', () => {
    document.getElementById('modal-guia').classList.add('oculto');
});

// --- 4. FILTROS, BUSCA E ATALHOS ---
function atualizarSelect() {
    const select = document.getElementById('select-veiculo');
    const busca = document.getElementById('input-busca').value.toLowerCase().trim();

    select.innerHTML = '<option value="">-- Escolha um modelo para simular --</option>';

    const filtrados = frotaCompleta.filter(v => {
        const okCat =
            categoriaAtual === 'Todos' ||
            (categoriaAtual === 'Compensa' && v.compensa) ||
            v.categoria === categoriaAtual;

        const okBusca =
            v.nome.toLowerCase().includes(busca) ||
            v.motor.toLowerCase().includes(busca);

        return okCat && okBusca;
    });

    filtrados.forEach(v => {
        const opt = document.createElement('option');
        opt.value = v.id;
        opt.textContent = `[${v.categoria}] ${v.nome} (${v.cvOrig} cv)`;
        select.appendChild(opt);
    });

    if (filtrados.length === 0) {
        select.innerHTML = '<option value="">Nenhum modelo encontrado</option>';
    }
}

document.querySelectorAll('.btn-filtro').forEach(btn => {
    btn.addEventListener('click', () => {
        document.querySelectorAll('.btn-filtro').forEach(b => b.classList.remove('ativo'));
        btn.classList.add('ativo');
        categoriaAtual = btn.dataset.cat;
        atualizarSelect();
    });
});

document.getElementById('input-busca').addEventListener('input', atualizarSelect);

function gerarAtalhos() {
    const container = document.getElementById('atalhos-lista');
    const destaques = ['hilux_28', 'amarok_v6', 'golf_gti', 'bmw_320i', 's1000rr', 'crf_250f', 'ktm_350_excf'];

    destaques.forEach(id => {
        const v = frotaCompleta.find(item => item.id === id);
        if (v) {
            const chip = document.createElement('button');
            chip.className = 'chip-btn';
            chip.textContent = v.nome.split('(')[0].trim();
            chip.addEventListener('click', () => {
                document.getElementById('select-veiculo').value = v.id;
                veiculoAtual = v;
                renderizarTudo();
            });
            container.appendChild(chip);
        }
    });
}

// --- 5. SISTEMA DE ABAS DO PAINEL DE RESULTADOS ---
document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.addEventListener('click', () => {
        document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('ativo'));
        document.querySelectorAll('.tab-content').forEach(c => c.classList.add('oculto'));

        btn.classList.add('ativo');
        document.getElementById(btn.dataset.tab).classList.remove('oculto');
    });
});

// --- 6. SIMULAÇÃO, STAGES 1 A 4 E VIDA ÚTIL ---
document.getElementById('btn-analisar').addEventListener('click', () => {
    const id = document.getElementById('select-veiculo').value;
    if (!id) {
        alert('Por favor, selecione um modelo na lista!');
        return;
    }
    veiculoAtual = frotaCompleta.find(v => v.id === id);
    if (veiculoAtual) renderizarTudo();
});

document.querySelectorAll('.btn-stage').forEach(btn => {
    btn.addEventListener('click', () => {
        document.querySelectorAll('.btn-stage').forEach(b => b.classList.remove('ativo'));
        btn.classList.add('ativo');
        stageAtual = parseInt(btn.dataset.stage);
        if (veiculoAtual) renderizarTudo();
    });
});

function pegarDadosStage(v, stg) {
    if (stg === 1) return { cv: v.cvStg1, tq: v.tqStg1 };
    if (stg === 2) return { cv: v.cvStg2, tq: v.tqStg2 };
    if (stg === 3) return { cv: v.cvStg3, tq: v.tqStg3 };
    return { cv: v.cvStg4, tq: v.tqStg4 };
}

function renderizarTudo() {
    const painel = document.getElementById('painel-resultado');
    painel.classList.remove('oculto');

    // Cabeçalho
    document.getElementById('tag-categoria').textContent = veiculoAtual.categoria;
    document.getElementById('tag-tracao').textContent = `TRAÇÃO: ${veiculoAtual.tracao}`;
    document.getElementById('tag-peso').textContent = `${veiculoAtual.pesoKg} KG`;
    document.getElementById('titulo-veiculo').textContent = veiculoAtual.nome;
    document.getElementById('subtitulo-motor').textContent = `Motor: ${veiculoAtual.motor}`;

    const selo = document.getElementById('selo-veredito');
    if (veiculoAtual.compensa) {
        selo.textContent = '✓ COMPENSA PREPARAR';
        selo.className = 'veredito-tag sim';
    } else {
        selo.textContent = '✕ BAIXO CUSTO-BENEFÍCIO';
        selo.className = 'veredito-tag nao';
    }

    const stgInfo = dadosStages[stageAtual];
    document.getElementById('desc-stage-atual').textContent = stgInfo.texto;

    const prep = pegarDadosStage(veiculoAtual, stageAtual);
    const ganhoCv = prep.cv - veiculoAtual.cvOrig;
    const ganhoTq = +(prep.tq - veiculoAtual.tqOrig).toFixed(1);
    const pctCv = ((ganhoCv / veiculoAtual.cvOrig) * 100).toFixed(1);
    const pctTq = ((ganhoTq / veiculoAtual.tqOrig) * 100).toFixed(1);

    const pesoPotOrig = (veiculoAtual.pesoKg / veiculoAtual.cvOrig).toFixed(2);
    const pesoPotPrep = (veiculoAtual.pesoKg / prep.cv).toFixed(2);

    // Ajuste de custo base se for moto (peças costumam ser mais baratas que de carro/camionete)
    const isMoto = veiculoAtual.categoria.includes('Moto');
    const custoEst = isMoto ? Math.round(stgInfo.custoBase * 0.65) : stgInfo.custoBase;
    const custoPorCv = ganhoCv > 0 ? Math.round(custoEst / ganhoCv) : 0;

    // KPIs
    document.getElementById('kpi-cv-final').textContent = `${prep.cv} cv`;
    document.getElementById('kpi-cv-ganho').textContent = `+${ganhoCv} cv (+${pctCv}%)`;

    document.getElementById('kpi-tq-final').textContent = `${prep.tq} kgfm`;
    document.getElementById('kpi-tq-ganho').textContent = `+${ganhoTq} kgfm (+${pctTq}%)`;

    document.getElementById('kpi-peso-pot').textContent = `${pesoPotPrep} kg/cv`;
    document.getElementById('kpi-peso-orig').textContent = `Original: ${pesoPotOrig} kg/cv`;

    document.getElementById('kpi-custo-cv').textContent = `R$ ${custoPorCv}/cv`;
    document.getElementById('kpi-investimento').textContent = `Investimento est.: R$ ${custoEst.toLocaleString('pt-BR')}`;

    // Barras de Dinamômetro (escala dinâmica baseada no Stage 4 do próprio veículo para ficar sempre visualmente claro)
    const tetoCv = Math.ceil(veiculoAtual.cvStg4 * 1.1);
    const tetoTq = Math.ceil(veiculoAtual.tqStg4 * 1.1);

    document.getElementById('lbl-stage-cv').textContent = `Stage ${stageAtual}`;
    document.getElementById('lbl-stage-tq').textContent = `Stage ${stageAtual}`;

    document.getElementById('val-cv-orig').textContent = `${veiculoAtual.cvOrig} cv`;
    document.getElementById('val-cv-prep').textContent = `${prep.cv} cv`;
    document.getElementById('badge-cv').textContent = `+${ganhoCv} cv`;

    document.getElementById('val-tq-orig').textContent = `${veiculoAtual.tqOrig} kgfm`;
    document.getElementById('val-tq-prep').textContent = `${prep.tq} kgfm`;
    document.getElementById('badge-tq').textContent = `+${ganhoTq} kgfm`;

    document.getElementById('bar-cv-orig').style.width = `${Math.min((veiculoAtual.cvOrig / tetoCv) * 100, 100)}%`;
    document.getElementById('bar-cv-prep').style.width = `${Math.min((prep.cv / tetoCv) * 100, 100)}%`;
    document.getElementById('bar-tq-orig').style.width = `${Math.min((veiculoAtual.tqOrig / tetoTq) * 100, 100)}%`;
    document.getElementById('bar-tq-prep').style.width = `${Math.min((prep.tq / tetoTq) * 100, 100)}%`;

    // Aba 2: Vida Útil
    let vu = stgInfo.vidaUtilBase;
    if (!veiculoAtual.compensa && stageAtual >= 2) vu -= 12;
    if (parseFloat(pctCv) > 70) vu -= 5;
    vu = Math.max(vu, 35);

    const vuBarra = document.getElementById('vu-barra');
    const vuNota = document.getElementById('vu-nota');
    vuBarra.style.width = `${vu}%`;
    vuNota.textContent = `${vu}% DE VIDA ÚTIL ESTIMADA`;

    if (vu >= 85) {
        vuBarra.style.backgroundColor = 'var(--green)';
        vuNota.style.backgroundColor = 'var(--green-soft)';
        vuNota.style.color = 'var(--green)';
    } else if (vu >= 65) {
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
        veiculoAtual.motor.toLowerCase().includes('diesel')
            ? 'Diesel S10 aditivado de alta qualidade'
            : (stageAtual >= 2 ? 'Gasolina Podium / Octapro ou Etanol' : 'Gasolina Aditivada');

    // Aba 3: Receita
    document.getElementById('texto-motivo').textContent = veiculoAtual.motivo;
    document.getElementById('texto-setup').textContent = veiculoAtual.recomendacao;

    // Atualiza Comparador caso esteja aberto
    atualizarComparador();
    painel.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

// --- 7. COMPARADOR ---
function preencherComparador() {
    const selA = document.getElementById('comp-veiculo-a');
    const selB = document.getElementById('comp-veiculo-b');

    frotaCompleta.forEach(v => {
        selA.add(new Option(`[${v.categoria}] ${v.nome}`, v.id));
        selB.add(new Option(`[${v.categoria}] ${v.nome}`, v.id));
    });

    if (frotaCompleta.length > 26) {
        selA.selectedIndex = 0;  // Hilux
        selB.selectedIndex = 25; // Golf GTI
    }

    selA.addEventListener('change', atualizarComparador);
    selB.addEventListener('change', atualizarComparador);
    atualizarComparador();
}

function atualizarComparador() {
    const vA = frotaCompleta.find(v => v.id === document.getElementById('comp-veiculo-a').value);
    const vB = frotaCompleta.find(v => v.id === document.getElementById('comp-veiculo-b').value);
    if (!vA || !vB) return;

    document.getElementById('comp-resultado').innerHTML = [vA, vB].map(v => {
        const d = pegarDadosStage(v, stageAtual);
        const vu = dadosStages[stageAtual].vidaUtilBase - (!v.compensa && stageAtual >= 2 ? 12 : 0);
        return `
            <div class="comp-card">
                <h4>${v.nome}</h4>
                <div class="comp-line"><span>Categoria:</span><strong>${v.categoria}</strong></div>
                <div class="comp-line"><span>Potência Stage ${stageAtual}:</span><strong>${d.cv} cv (+${d.cv - v.cvOrig})</strong></div>
                <div class="comp-line"><span>Torque Stage ${stageAtual}:</span><strong>${d.tq} kgfm (+${(d.tq - v.tqOrig).toFixed(1)})</strong></div>
                <div class="comp-line"><span>Peso / Potência:</span><strong>${(v.pesoKg / d.cv).toFixed(2)} kg/cv</strong></div>
                <div class="comp-line"><span>Vida Útil:</span><strong>${vu}%</strong></div>
                <div class="comp-line"><span>Compensa?</span><strong>${v.compensa ? '✓ Sim' : '✕ Não'}</strong></div>
            </div>
        `;
    }).join('');
}