package tocamidi;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import javax.sound.midi.*;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/*
 * A classe eventos é responsável por mexer diretamente com
 * os arquivos Midi, com o sequenciador, com a sequencia, com
 * as trilhas e com as mensagens midi. Esta classe que coloca
 * a música para tocar
 */

public class Eventos {
	private Sequencer sequenciador;
	private Sequence  sequencia;
	private Receiver receptor;
	private File arqmidi;
	private int volumeAtual;
	private int aceleracao;
	private int andamento;
    private String metro;
	
	public Eventos(){
		volumeAtual = 70; // Começa o volume com 70
		aceleracao = 0; // começa a aceleração com 0
	}
	
	public void iniciaSequenciador(){
		if(sequenciador != null){
			if (sequenciador.isOpen()){				
				stop(); // Para a execução com a sequência atual para criar uma nova
			}
		}
		
		try{
			// Inicia o sequenciador
			if(arqmidi != null){
				sequencia = MidiSystem.getSequence(arqmidi);
				sequenciador = MidiSystem.getSequencer();
				sequenciador.setSequence(sequencia);
				sequenciador.open();
				
				try {
					receptor = sequenciador.getTransmitters().iterator().next().getReceiver(); // inicializa o receptor
					sequenciador.getTransmitter().setReceiver(receptor); // configura o receptor no sequenciador
				} catch (MidiUnavailableException e) {}
			}
			else{
				throw new IOException();
			}
			
			setVolume(volumeAtual); // Começa com o valor da barra de volume

		}
		catch(MidiUnavailableException e1) { 
			JOptionPane.showMessageDialog(null, e1+" : Dispositivo midi não disponível.");
		}
		catch(InvalidMidiDataException e2){
			JOptionPane.showMessageDialog(null, e2+" : Erro nos dados midi.");
		}
		catch(IOException e3){
			JOptionPane.showMessageDialog(null, e3+ " : Erro: arquivo não existe");
		}
	}
	
	public void play(){
		// Checa se um sequenciador foi configurado
		if(sequenciador != null){
			if(sequenciador.getMicrosecondPosition() == sequenciador.getMicrosecondLength()){
				stop(); // Para fechar o sequenciadorse a música tiver acabado
			}
			
			if (!sequenciador.isOpen()){ // Vê se o sequenciador está aberto e rodando
				iniciaSequenciador(); /// Iniciar de novo pro caso de ter sido dado stop ou a música ter acabado
			}
			setVolume(volumeAtual); // Começa o volume com o valor da barra de volume
			sequenciador.start(); // Começa/volta a tocar
		}
		else{ // Não deixa dar play sem um sequenciador de um arquivo midi estar configurado
			JOptionPane.showMessageDialog(null, "Selecione um arquivo midi para tocar!");
		}

	}
	
	public void pause(){
		if(sequenciador != null)
			sequenciador.stop(); // Para de tocar até que se aperte play de novo
	}
	
	public void stop(){
		if(sequenciador != null)
			sequenciador.close(); // Para de tocar
	}
	
	public boolean escolheArquivo(){
		// Checa se o sequenciador já foi instanciado
		if(sequenciador != null){
			if(sequenciador.isRunning()){
				int resposta = JOptionPane.showConfirmDialog(null, "Aviso: Se selecionar outra m�sica, a atual vai parar de tocar. Deseja continuar?");
				if (resposta != JOptionPane.YES_OPTION){
					return false; // Sai do método sem fazer nada se o usuário não quiser trocar a música
				}
			}
		}
		File arq;
		JFileChooser escolha = new JFileChooser();
		
		FileNameExtensionFilter filtro = new FileNameExtensionFilter("Arquivos Midi", "mid", "midi");
		escolha.setFileFilter(filtro);
		
		File dir = new File(System.getProperty("user.dir")); 
		escolha.setCurrentDirectory(dir); // Faz com que comece na pasta do projeto
		
		if(escolha.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
			arq = escolha.getSelectedFile();
			arqmidi = arq;
			
			iniciaSequenciador();
			
			return true; // Retorna true se tiver sido escolhido um arquivo
		}
		else{
			return false; // Retorna false se nenhum arquivo tiver sido escolhido
		}
	}
	
	public Sequence getSequencia(){
		return sequencia;
	}
	
	public int getVolume(){
		return volumeAtual;
	}
	
	public String getNome(){
		return arqmidi.getName();
	}
	
	public int getAceleracao(){
		return aceleracao;
	}
	
	public int getAndamento(){
		return andamento;
	}
	
	public int getTotalSegundos(){ // Retorna em segundos
		long duracao     = (sequencia.getMicrosecondLength())/1000000;
		int total = (int) duracao;
		
		return total;
	}
	
	public String getSTempoTotal(){
		long duracao     = sequencia.getMicrosecondLength()/1000000;
		int s = 0, m = 0, h = 0;
		
		while (duracao > 0){
			s++;
			if(s >= 60){
				s = 0;
				m++;
				if (m >= 60){
					m = 0;
					h++;
				}
			}
			duracao--;
		}
		
		
		return (Contador.paraString(h) + ":" + Contador.paraString(m) + ":" + Contador.paraString(s));
	}
	
	public void setAndamento(int valor){
		if(sequenciador != null){
			andamento = valor;
			
			if(andamento >= 10 && andamento <= 400) // Impede andamentos negativos, muito baixos ou muito altos
				sequenciador.setTempoInBPM((float) andamento); // Atualiza o andamento da música
		}
	}
	
	public void setAceleracao(int fator){
		if(sequenciador != null){
			andamento = (int) sequenciador.getTempoInBPM(); // Pega o andamento atual da musica
			
			aceleracao += fator; // Salva a aceleracao (positiva ou negativa) atual
			andamento += fator; // Aumenta ou diminui, de acordo com o fator
			
			if(andamento >= 10 && andamento <= 400) // Impede andamentos negativos, muito baixos ou muito altos
				sequenciador.setTempoInBPM((float) andamento); // Atualiza o andamento da música
		}
		
	}
	
	public void setVolume(int valor){
		volumeAtual = valor;
		ShortMessage mensagemDeVolume = new ShortMessage();
		for(int i=0; i<16; i++)
		{
			try{ 
				mensagemDeVolume.setMessage(ShortMessage.CONTROL_CHANGE, i, 7, valor);
				receptor.send(mensagemDeVolume, -1);
			}
			catch (InvalidMidiDataException ignorar) {}
		}
	}
	
	public void setPosicao(long valor){
		if(sequenciador != null)
			sequenciador.setMicrosecondPosition(valor); // Altera a posição em microssegundos da música
	}
	
	
	public boolean isOpen(){
		return (arqmidi != null);
	}
	
	public boolean tocando(){
		return sequenciador.isRunning();
	}
	
	public Dimension getFormulaDeCompasso(Track trilha)
    {   
		int p = 1;
        int q = 1;
        
        final int FORMULA_DE_COMPASSO = 0x58;

        for(int i=0; i<trilha.size(); i++)
        {
          MidiMessage m = trilha.get(i).getMessage();
          if(m instanceof MetaMessage) 
          {
            if(((MetaMessage)m).getType()==FORMULA_DE_COMPASSO)
            {
                MetaMessage mm = (MetaMessage)m;
                byte[] data = mm.getData();
                p = data[0];
                q = data[1];
                
                metro = setMetro(q);
                
                return new Dimension(p,q);
            }
          }
        }
        return new Dimension(p,q);
    }
	
	public String getTonalidade(Track trilha)
    {
	  	final int MENSAGEM_TONALIDADE = 0x59; 
		String stonalidade = "Não informa";
	       
		for(int i=0; i<trilha.size(); i++)
		{ 
			MidiMessage m = trilha.get(i).getMessage();
	           
	       try // Usa o try para não dar erro de execução e cair no catch caso haja problemas no casting
	       {
				MetaMessage mm        = (MetaMessage)m;
				if (mm.getType() == MENSAGEM_TONALIDADE)    
				   {
				        byte[]     data       = mm.getData();
				        byte       tonalidade = data[0];
				        byte       maior      = data[1];
	
				        String       smaior = "Maior";
				        if(maior==1) smaior = "Menor";
	
				        if(smaior.equalsIgnoreCase("Maior"))
				        {
				            switch (tonalidade)
				            {
				                case -7: stonalidade = "Dób Maior"; break;
				                case -6: stonalidade = "Solb Maior"; break;
				                case -5: stonalidade = "Réb Maior"; break;
				                case -4: stonalidade = "Láb Maior"; break;
				                case -3: stonalidade = "Mib Maior"; break;
				                case -2: stonalidade = "Sib Maior"; break;
				                case -1: stonalidade = "Fá Maior"; break;
				                case  0: stonalidade = "Dó Maior"; break;
				                case  1: stonalidade = "Sol Maior"; break;
				                case  2: stonalidade = "Ré Maior"; break;
				                case  3: stonalidade = "Lá Maior"; break;
				                case  4: stonalidade = "Mi Maior"; break;
				                case  5: stonalidade = "Si Maior"; break;
				                case  6: stonalidade = "Fá# Maior"; break;
				                case  7: stonalidade = "Dó# Maior"; break;
				            }
				        }
	
				        else if(smaior.equalsIgnoreCase("Menor"))
				        {
				            switch (tonalidade)
				            {
				                case -7: stonalidade = "Láb Menor"; break;
				                case -6: stonalidade = "Mib Menor"; break;
				                case -5: stonalidade = "Sib Menor"; break;
				                case -4: stonalidade = "Fá Menor"; break;
				                case -3: stonalidade = "Dó Menor"; break;
				                case -2: stonalidade = "Sol Menor"; break;
				                case -1: stonalidade = "Ré Menor"; break;
				                case  0: stonalidade = "Lá Menor"; break;
				                case  1: stonalidade = "Mi Menor"; break;
				                case  2: stonalidade = "Si Menor"; break;
				                case  3: stonalidade = "Fá# Menor"; break;
				                case  4: stonalidade = "Dó# Menor"; break;
				                case  5: stonalidade = "Sol# Menor"; break;
				                case  6: stonalidade = "Ré# Menor"; break;
				                case  7: stonalidade = "Lá# Menor"; break;
				            }
				        }
					  }
			} catch (Exception ignora) {}
	  }
      return stonalidade;
    }
        
    public String getMetro(){
        return metro;
    }
        
    public String setMetro(int valor){
        switch(valor){
            case 0: return "semibreve";
            case 1: return "minima";
            case 2: return "seminima";
            case 3: return "colcheia";
            case 4: return "semicolcheia";
            case 5: return "fusa";
            case 6: return "semifusa";

            default: return "Não informa";
        }
    }
	
}
