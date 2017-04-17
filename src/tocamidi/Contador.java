package tocamidi;

import javax.swing.SwingWorker;

/*
 * Classe responsável por contar o tempo em uma thread
 * separada, de acordo com as definições herdadas da 
 * classe SwingWorker
 */
public class Contador extends SwingWorker<Void, Void>{
	
	private int horas;
	private int minutos;
	private int segundos;
	private int tempoAtualSegundos;
	private int tempoLimite;
	private boolean on;
	private boolean pause;
		
	public Contador(int comeco, int limite){
		tempoAtualSegundos = comeco;
		tempoLimite = limite;
		on = false;
		pause = false;
	}
	
	public int getSegundos(){
		return segundos;
	}
	
	public int getMinutos(){
		return minutos;
	}
	
	public int getHoras(){
		return horas;
	}
	
	public int getAtualSegundos(){
		return tempoAtualSegundos;
	}
	
	public String getSTempoAtual(){
		return ( paraString(horas) + ":" + paraString(minutos) + ":" + paraString(segundos) );
	}
	
	public boolean pausado(){ // Retorna true se estiver pausado
		return pause;
	}
	
	public void setTempo(int valor){ 
		tempoAtualSegundos = valor;
		converteTempo();
	}
	
	private void converteTempo(){
		segundos = 0; minutos = 0; horas = 0; // reseta
		
		int t = tempoAtualSegundos;
		
		while (t > 0){
			segundos++;
			if(segundos >= 60){
				segundos = 0;
				minutos++;
				if (minutos >= 60){
					minutos = 0;
					horas++;
				}
			}
			t--;
		}
	}
	
	public void reseta(){
		 tempoAtualSegundos = 0;
         tempoLimite = 0;
         horas = 0;
         minutos = 0;
         segundos = 0;
         on = false;
         pause = false;
	}
	
	public void setOn(){
		on = true;
	}
	
	public void setOff(){
		on = false;
	}
	
	public void pause(){
		pause = true;
	}
	
	public void play(){
		pause = false;
	}
	
	public static String paraString(int valor){
		if(valor < 10){
			return "0" + valor;	
		}
		else{
			return "" + valor;
		}
	}

	/*
	 * Coloca-se os segundos no atributo progress porque este não aceita
	 * valores maiores do que 100 sem sair deste método, então
	 * o responsável pela condição de saída do método é a condição
	 * deste laço, que acontece quando o tempo atual da música atingir
	 * o limite (final) da mesma. A atualização da barra de progressos
	 * da classe Janela se dá a partir do método getAtualSegundos()
	 * */
	@Override
	protected Void doInBackground() throws Exception {
		setProgress(0);
		while(on && (tempoAtualSegundos < tempoLimite)){
			if(!pause){ // Enquanto pause estiver ativo, para o progresso				
					
				setProgress(segundos);
				
				tempoAtualSegundos++;
				segundos++;
				if (segundos == 60){
					segundos = 0;
					minutos++;
					
					if(minutos == 60){
						minutos = 0;
						horas++;
					}
				}

				try{
					Thread.sleep(1000); // Intervalo de 1 segundo
				}catch(Exception ignorar) {}
				
			}
			
		}
		
		return null;
	}

	/*
	 * Este método é invocado quando o método doInBackGround se encerra
	 */
	 @Override
     public void done() {
		 reseta(); // Reseta o contador quando acabar a música
     }
	
}