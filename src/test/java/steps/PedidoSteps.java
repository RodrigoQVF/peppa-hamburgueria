package steps;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Entao;
import io.cucumber.datatable.DataTable;
import peppa.hamburgueria.CardapioService;
import peppa.hamburgueria.PedidoService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class PedidoSteps {

    private CardapioService cardapioService;
    private PedidoService pedidoService;
    private double valorTotal;
    private String mensagemErro;
    private int tempoEstimado;
    private int quantidadeTotal;

    @Dado("que o cardápio contém os itens:")
    public void que_o_cardapio_contem_os_itens(DataTable dataTable) {
        cardapioService = new CardapioService();
        pedidoService = new PedidoService(cardapioService);
        List<Map<String, String>> itens = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> item : itens) {
            String nome = item.get("item");
            double preco = Double.parseDouble(item.get("preco"));
            cardapioService.cadastrarItem(nome, preco);
        }
    }

    @Quando("eu peço {string} com {int} unidades")
    public void eu_peco_com_unidades(String item, int quantidade) {
        try {
            valorTotal = pedidoService.calcularTotal(item, quantidade);
            quantidadeTotal = quantidade;
            mensagemErro = null;
        } catch (IllegalArgumentException e) {
            mensagemErro = e.getMessage();
        }
    }

    @Entao("o valor total do pedido deve ser {string}")
    public void o_valor_total_do_pedido_deve_ser(String valorEsperadoStr) {
        double valorEsperado = Double.parseDouble(valorEsperadoStr.replace(",", "."));
        assertEquals(valorEsperado, valorTotal, 0.01, "O valor total calculado está incorreto.");
    }

    @Entao("deve ser exibida a mensagem {string}")
    public void deve_ser_exibida_a_mensagem(String mensagemEsperada) {
        assertNotNull(mensagemErro, "Nenhuma mensagem de erro foi exibida.");
        assertEquals(mensagemEsperada, mensagemErro, "A mensagem de erro está incorreta.");
    }

    @Quando("eu peço {int} {string} e {int} {string}")
    public void eu_peco_e(int qtd1, String item1, int qtd2, String item2) {
        quantidadeTotal = qtd1 + qtd2;
        tempoEstimado = pedidoService.calcularTempoEstimado(quantidadeTotal);
    }

    @Entao("o tempo estimado de preparo deve ser {int} minutos")
    public void o_tempo_estimado_de_preparo_deve_ser_minutos(int tempoEsperado) {
        assertEquals(tempoEsperado, tempoEstimado, "O tempo estimado de preparo está incorreto.");
    }

    @Quando("eu peço {string} com {int} unidade")
    public void eu_peco_com_unidade(String item, int quantidade) {
        eu_peco_com_unidades(item, quantidade);
    }

    @Quando("aplico um desconto de {int} por cento")
    public void e_aplico_um_desconto_de_por_cento(int percentualDesconto) {
        if (mensagemErro == null) {
            double desconto = valorTotal * (percentualDesconto / 100.0);
            valorTotal = valorTotal - desconto;
            valorTotal = Math.round(valorTotal * 100.0) / 100.0;
        } else {
            fail("Não é possível aplicar desconto, pois o pedido inicial falhou com a mensagem: " + mensagemErro);
        }
    }
}
