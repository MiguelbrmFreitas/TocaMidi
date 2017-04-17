package tocamidi;

/*
 * Classe que contém o método principal.
 * Instancia uma janela e começa o programa.
 */
public class TocaMidi
{      
	public static void main(String args[]) 
	{        
			Janela janela = new Janela();
			janela.setDefaultCloseOperation(Janela.EXIT_ON_CLOSE);
			janela.pack();
			janela.setVisible(true);
	}  	
	
}