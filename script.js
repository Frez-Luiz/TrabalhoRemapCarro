let frotaCompleta = [];
let veiculoAtual = null;
let stageAtual = 1;
let categoriaAtual = 'Todos';
let tamanhoFonte = 16;

const dadosStages = {
    1: {
        texto: 'Stage 1: Calibração eletrônica da central (ECU). Mantém o escapamento, o sistema de admissão e os componentes internos originais.',
        vidaUtilBase: 96,
        custoBase: 2000,
        oleo: 'Troca no prazo especificado pelo fabricante ou a cada 8.000 km (motocicletas: 3.000 km)',
        alerta: 'O conjunto mecânico original opera rigorosamente dentro da margem térmica e de pressão segura.'
    },
    2: {
        texto: 'Stage 2: Reprogramação eletrônica associada à instalação de downpipe/escapamento esportivo e filtro de ar de alto fluxo.',
        vidaUtilBase: 89,
        custoBase: 4500,
        oleo: 'Reduzir o intervalo de troca em 20%, utilizando lubrificante 100% sintético homologado',
        alerta: 'Manter a higienização periódica do filtro de ar e utilizar combustível de alta octanagem.'
    },
    3: {
        texto: 'Stage 3: Substituição do turbocompressor/compressor por unidade de maior vazão (ou comandos e injetores em motocicletas).',
        vidaUtilBase: 73,
        custoBase: 15500,
        oleo: 'A cada 5.000 km (motocicletas: a cada 15 horas de operação ou 1.500 km)',
        alerta: 'Recomenda-se calibração da transmissão ou reforço de embreagem e revisão do sistema de arrefecimento.'
    },
    4: {
        texto: 'Stage 4: Preparação de alta performance com componentes internos forjados (pistões e bielas) e calibração dedicada para pista.',
        vidaUtilBase: 54,
        custoBase: 35000,
        oleo: 'A cada 3.000 km ou imediatamente após eventos de pista e competição',
        alerta: 'Exige manutenção preventiva rigorosa devido ao desgaste acelerado de transmissão e pneus.'
    }
};

const artigosBlog = {
    combustivel: {
        titulo: '⛽ Qual combustível utilizar após o Stage 1?',
        html: `
            <p><strong>1. Motores Turbo a Gasolina ou Flex:</strong> Ao elevar a pressão de sobrealimentação e otimizar o avanço de ignição, a temperatura na câmara de combustão aumenta. O uso de gasolina comum pode provocar <em>pré-detonação</em>, levando a central eletrônica a recuar o ponto e limitar a potência.</p>
            <p><strong>Recomendação Técnica:</strong> Utilize preferencialmente <strong>Gasolina de Alta Octanagem (Podium ou Octapro)</strong> ou <strong>Etanol</strong>, que apresenta elevada resistência à detonação e auxilia no resfriamento da admissão.</p>
            <p><strong>2. Camionetes Turbo Diesel:</strong> Abasteça exclusivamente com <strong>Diesel S10 aditivado</strong> de procedência comprovada e substitua o filtro de combustível a cada 10.000 km para preservar o sistema de injeção.</p>
        `
    },
    velas: {
        titulo: '⚡ Velas e bobinas: quando substituir antes do remap?',
        html: `
            <p>Com maior massa de ar e combustível comprimida no interior dos cilindros, a resistência elétrica para a formação da centelha aumenta significativamente. Velas ou bobinas desgastadas que operavam normalmente no mapa original podem apresentar falhas de ignição (<em>misfire</em>) em plena carga.</p>
            <p><strong>Diretriz FrezPreparações:</strong> Caso as velas possuam mais de <strong>25.000 km</strong> de uso, recomenda-se a substituição antes da reprogramação. Para projetos Stage 2 e Stage 3, indicamos velas um grau térmico mais frias (como NGK Iridium Grau 8 ou 9) para melhor dissipação térmica.</p>
        `
    },
    piggyback: {
        titulo: '🔌 Diferença entre Piggyback e Remap de ECU',
        html: `
            <p><strong>Piggyback (Módulo Externo):</strong> Dispositivo conectado aos chicotes dos sensores (como MAP e Boost) que intercepta e modifica os sinais enviados à central original para elevar a pressão do turbo. Embora seja de fácil remoção, não gerencia mapas de ponto de ignição, limitadores nem calibração de câmbio.</p>
            <p><strong>Remap de ECU (Reprogramação Direta):</strong> Consiste na leitura e calibração direta do software da unidade de controle eletrônico, ajustando dezenas de parâmetros (pressão, mistura ar/combustível, ponto, limitadores e resposta do acelerador) de forma integrada, segura e precisa.</p>
        `
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

// --- 2. BOTÃO VOLTAR AO TOPO ---
const btnVoltarTopo = document.getElementById('btn-voltar-topo');

window.addEventListener('scroll', () => {
    if (window.scrollY > 320) {
        btnVoltarTopo.classList.remove('oculto');
    } else {
        btnVoltarTopo.classList.add('oculto');
    }
});

btnVoltarTopo.addEventListener('click', () => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
});

// --- 3. TEMA CLARO / ESCURO & FONTE ---
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

// --- 4. MODAIS (GUIA E ARTIGOS) ---
document.getElementById('btn-guia').addEventListener('click', () => {
    document.getElementById('modal-guia').classList.remove('oculto');
});
document.getElementById('btn-fechar-guia').addEventListener('click', () => {
    document.getElementById('modal-guia').classList.add('oculto');
});

document.querySelectorAll('.btn-ler-artigo').forEach(btn => {
    btn.addEventListener('click', () => {
        const art = artigosBlog[btn.dataset.artigo];
        if (art) {
            document.getElementById('artigo-titulo').textContent = art.titulo;
            document.getElementById('artigo-corpo').innerHTML = art.html;
            document.getElementById('modal-artigo').classList.remove('oculto');
        }
    });
});
document.getElementById('btn-fechar-artigo').addEventListener('click', () => {
    document.getElementById('modal-artigo').classList.add('oculto');
});

// --- 5. FILTROS, BUSCA E ATALHOS ---
function atualizarSelect() {
    const select = document.getElementById('select-veiculo');
    const busca = document.getElementById('input-busca').value.toLowerCase().trim();

    select.innerHTML = '<option value="">-- Selecione um modelo para análise --</option>';

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

// --- 6. ABAS DO PAINEL DE RESULTADOS ---
document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.addEventListener('click', () => {
        document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('ativo'));
        document.querySelectorAll('.tab-content').forEach(c => c.classList.add('oculto'));

        btn.classList.add('ativo');
        document.getElementById(btn.dataset.tab).classList.remove('oculto');
    });
});

document.querySelectorAll('.extra-check').forEach(chk => {
    chk.addEventListener('change', () => {
        if (veiculoAtual) renderizarTudo();
    });
});

// --- 7. SIMULAÇÃO, STAGES, EXTRAS E CONFIABILIDADE ---
document.getElementById('btn-analisar').addEventListener('click', () => {
    const id = document.getElementById('select-veiculo').value;
    if (!id) {
        alert('Por favor, selecione um modelo na lista.');
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

    document.getElementById('tag-categoria').textContent = veiculoAtual.categoria;
    document.getElementById('tag-tracao').textContent = `TRAÇÃO: ${veiculoAtual.tracao}`;
    document.getElementById('tag-peso').textContent = `${veiculoAtual.pesoKg} KG`;
    document.getElementById('titulo-veiculo').textContent = veiculoAtual.nome;
    document.getElementById('subtitulo-motor').textContent = `Motorização: ${veiculoAtual.motor}`;

    const selo = document.getElementById('selo-veredito');
    if (veiculoAtual.compensa) {
        selo.textContent = '✓ VIABILIDADE RECOMENDADA';
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

    let custoExtras = 0;
    let desgasteExtras = 0;
    document.querySelectorAll('.extra-check:checked').forEach(chk => {
        custoExtras += parseInt(chk.dataset.preco);
        desgasteExtras += parseInt(chk.dataset.desgaste);
    });

    const isMoto = veiculoAtual.categoria.includes('Moto');
    const custoBase = isMoto ? Math.round(stgInfo.custoBase * 0.65) : stgInfo.custoBase;
    const custoTotal = custoBase + custoExtras;
    const custoPorCv = ganhoCv > 0 ? Math.round(custoTotal / ganhoCv) : 0;

    document.getElementById('kpi-cv-final').textContent = `${prep.cv} cv`;
    document.getElementById('kpi-cv-ganho').textContent = `+${ganhoCv} cv (+${pctCv}%)`;

    document.getElementById('kpi-tq-final').textContent = `${prep.tq} kgfm`;
    document.getElementById('kpi-tq-ganho').textContent = `+${ganhoTq} kgfm (+${pctTq}%)`;

    document.getElementById('kpi-peso-pot').textContent = `${pesoPotPrep} kg/cv`;
    document.getElementById('kpi-peso-orig').textContent = `Original: ${pesoPotOrig} kg/cv`;

    document.getElementById('kpi-investimento').textContent = `R$ ${custoTotal.toLocaleString('pt-BR')}`;
    document.getElementById('kpi-custo-cv').textContent = `R$ ${custoPorCv} por cv ganho`;

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

    let vu = stgInfo.vidaUtilBase - desgasteExtras;
    if (!veiculoAtual.compensa && stageAtual >= 2) vu -= 12;
    if (parseFloat(pctCv) > 70) vu -= 5;
    vu = Math.min(Math.max(vu, 30), 99);

    const vuBarra = document.getElementById('vu-barra');
    const vuNota = document.getElementById('vu-nota');
    vuBarra.style.width = `${vu}%`;
    vuNota.textContent = `${vu}% DE CONFIABILIDADE ESTIMADA`;

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
            ? 'Diesel S10 aditivado de procedência comprovada'
            : (stageAtual >= 2 ? 'Gasolina Podium / Octapro ou Etanol' : 'Gasolina Aditivada');

    document.getElementById('texto-motivo').textContent = veiculoAtual.motivo;
    document.getElementById('texto-setup').textContent = veiculoAtual.recomendacao;

    atualizarComparador();
}

// --- 8. COMPARATIVO TÉCNICO ---
function preencherComparador() {
    const selA = document.getElementById('comp-veiculo-a');
    const selB = document.getElementById('comp-veiculo-b');

    frotaCompleta.forEach(v => {
        selA.add(new Option(`[${v.categoria}] ${v.nome}`, v.id));
        selB.add(new Option(`[${v.categoria}] ${v.nome}`, v.id));
    });

    if (frotaCompleta.length > 26) {
        selA.selectedIndex = 0;
        selB.selectedIndex = 25;
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
                <div class="comp-line"><span>Potência Stage ${stageAtual}:</span><strong class="mono">${d.cv} cv (+${d.cv - v.cvOrig})</strong></div>
                <div class="comp-line"><span>Torque Stage ${stageAtual}:</span><strong class="mono">${d.tq} kgfm (+${(d.tq - v.tqOrig).toFixed(1)})</strong></div>
                <div class="comp-line"><span>Peso / Potência:</span><strong class="mono">${(v.pesoKg / d.cv).toFixed(2)} kg/cv</strong></div>
                <div class="comp-line"><span>Confiabilidade:</span><strong class="mono">${vu}%</strong></div>
                <div class="comp-line"><span>Viabilidade:</span><strong>${v.compensa ? '✓ Recomendada' : '✕ Baixo retorno'}</strong></div>
            </div>
        `;
    }).join('');
}