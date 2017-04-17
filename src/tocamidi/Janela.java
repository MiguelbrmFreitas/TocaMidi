package tocamidi;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import javax.swing.*;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/*
 * A classe Janela herda da classe JFrame, que torna possível a criação de
 * uma janela visível na tela do usuário. Além de herdar todos os componentes
 * de JFrame, se constrói aqui o meu JFrame próprio, com componentes específicos
 * para a interface gráfica do Tocador Midi.
 * 
 * @autor Miguel
 */
public class Janela extends JFrame implements ActionListener, ChangeListener, MouseListener, PropertyChangeListener{
	
	private static final long serialVersionUID = 1L; // Para tirar o warning
	
	private JPanel painel;
	private JSlider barravolume;
	private JProgressBar progresso;
	private JTextArea dados;
	private JButton play, pause, stop, search, acelera, desacelera;
	private JLabel label1, label2;
	private Contador contador;
	private GridBagConstraints c;
	private Eventos eventos;
	
	public Janela(){
		super("Tocador Midi (por Miguel Freitas)"); // Carrega o método construtor do JFrame original
		
		// Chama métodos privados de configuração dos componentes da janela e de eventos
		
		eventos = new Eventos();
		dados = new JTextArea();
		
		configuraLabels();
		configuraBotoes();
		configuraBarraDeVolume(0, 127, eventos.getVolume());
		configuraDados();
		configuraProgresso(0,0);
		configuraPainel();
		
		add(painel); // Adiciona o painel à janela do usuário
	}
		
	
	private void configuraPainel(){
		painel = new JPanel();
		painel.setLayout(new GridBagLayout());
		
		JPanel painelaux = new JPanel();
		painelaux.add(play);
		painelaux.add(pause);
		painelaux.add(stop);
		painelaux.add(barravolume);
		painelaux.add(label2);
		
		JPanel painelaux2 = new JPanel();
		painelaux2.add(acelera);
		painelaux2.add(desacelera);
		painelaux2.add(label1);
		
		c = new GridBagConstraints();
		
		c.gridx = 0;		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		painel.add(search, c);
		
		c.gridx = 0;		c.gridy = 2;
		painel.add(painelaux, c);
		
		c.gridx = 0;		c.gridy = 4;
		painel.add(progresso, c);
		
		c.gridx = 0;		c.gridy = 5;
		painel.add(painelaux2, c);
		
		c.gridx = 0;		c.gridy = 7;
		painel.add(dados, c);
		
	}
	
	private void configuraLabels(){
		label1 = new JLabel("+ " + eventos.getAceleracao());
		label2 = new JLabel(new ImageIcon("imagens/volume.png"));
	}
	
	private void configuraBotoes(){		
		play = new JButton(new ImageIcon("imagens/play.png"));
		play.addActionListener(this);
		pause = new JButton(new ImageIcon("imagens/pause.png"));
		pause.addActionListener(this);
		stop = new JButton(new ImageIcon("imagens/stop.png"));
		stop.addActionListener(this);
		search = new JButton(new ImageIcon("imagens/folder4.png"));
		search.addActionListener(this);
		acelera = new JButton("Acelerar");
		acelera.addActionListener(this);
		desacelera = new JButton("Desacelerar");
		desacelera.addActionListener(this);
	}
	
	private void configuraBarraDeVolume(int min, int max, int inicial) {
		barravolume = new JSlider(JSlider.HORIZONTAL, min, max, inicial);
		
		barravolume.setPaintTicks(true);
		barravolume.setMinorTickSpacing(1);
		barravolume.setMajorTickSpacing(10);
		barravolume.setPaintLabels(true);
		barravolume.setSnapToTicks(true);
		barravolume.setPreferredSize(new Dimension(220,70));	
		barravolume.addChangeListener(this);
	}
	
	private void configuraProgresso(int min, int max){
		progresso = new JProgressBar(min, max);
		progresso.setValue(0);
		progresso.setStringPainted(true);
		progresso.setPreferredSize(new Dimension(500,30));
		progresso.setString("00:00:00"); // Começo
		progresso.addMouseListener(this);
	}
		
	private void configuraDados(){
		String texto;
		
		if(eventos.isOpen()){
			Sequence sequencia = eventos.getSequencia();
			Track trilha [] = sequencia.getTracks();			
			String nome = eventos.getNome();
			String tempo = eventos.getSTempoTotal();
			long duracao     = sequencia.getMicrosecondLength()/1000000;
			
			int  resolucao   = sequencia.getResolution();
			long totaltiques = sequencia.getTickLength();
			
			Dimension compasso = eventos.getFormulaDeCompasso(trilha[0]);
			int k = compasso.width;
			int metro = (int) Math.pow(2, compasso.height);
			
			float durtique       = (float)duracao/totaltiques;
			float durseminima    = durtique*resolucao;
			int bpm            =  (int) (60/durseminima);
			int   totalseminimas = (int)(duracao/durseminima);
			
			
			texto = "\n\n\tNome: " + nome + "\n\n\tDuracao: "+ tempo + "\n\t\u2669 = " + bpm + " bpm " + "\n\tMetro: " + eventos.getMetro() 
					+ "\n\tFormula de Compasso: "  + k + "/" + metro  +  "\n\tArmadura de tonalidade: "
					+ eventos.getTonalidade(trilha[0]) 	+ "\n\tResolucao: "	+ resolucao
					  + "\n\tTotal de tiques: " + totaltiques + "\n\tDuracao dos tiques: "
					  + durtique + "\n\tDuracao das seminimas: " + durseminima +
					  "\n\tTotal de seminimas: " + totalseminimas + "\n\n";
		}
		else{
			texto = "\n\n\t\tNenhum arquivo midi aberto.\n\n";
		}
		
		dados.removeAll(); // Remove textos anteriores, se houver
		dados.setText(texto); // Adiciona o texto com as informações do arquivo midi
		dados.setBackground((new JPanel().getBackground())); // Coloca na cor padrão de um JPanel
	}
	
	private void configuraContador(){
		progresso.setMaximum(eventos.getTotalSegundos());
		contador = new Contador(progresso.getValue(), eventos.getTotalSegundos());
		contador.setOn();
		contador.play();
		contador.addPropertyChangeListener(this);
		contador.execute();
	}

	// Chama os métodos da classe Eventos
	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		
		if(source == play){
			if(eventos.isOpen()){ // Só tenta se tiver um arquivo midi válido
				if(contador == null){ // Caso de primeira vez tocando uma música
					configuraContador();	
				}
				else if(!eventos.tocando() && !contador.pausado()){ // Caso de ter sido dado stop ou trocado a música
					configuraContador();
				}
				else if(!eventos.tocando() && contador.pausado()){ // Caso de pause
					contador.setOn();
					contador.play();
				}
			}
			
			if(contador != null)
				contador.setTempo(progresso.getValue()); 
			eventos.play();
			eventos.setPosicao( (long) (progresso.getValue()*1000000) ); // Começar a tocar de acordo com o valor da barra de progressos
			eventos.setAndamento(eventos.getAndamento()); // Toca no andamento especificado
		}
		else if(source == pause){
			eventos.pause();
			eventos.setAndamento(eventos.getAndamento()); // Volta a tocar no último andamento
			if(contador != null)
				contador.pause();
		}
		else if(source == stop){
			eventos.stop();				
			progresso.setValue(0);
			if(contador != null)
				contador.reseta();
		}
		else if(source == search){
			boolean escolha = eventos.escolheArquivo();
			
			if(escolha){ // Só faz isso se tiver escolhido um arquivo novo
				painel.remove(dados);
				remove(painel);
				configuraDados();
				configuraPainel();
				add(painel);
				pack();
				
				configuraContador();
				contador.reseta();
				progresso.setValue(0);	
			}
		}
		else if (source == acelera || source == desacelera){
			if(source == acelera){
				eventos.setAceleracao(5);
			}
			else{
				eventos.setAceleracao(-5);
			}
			remove(painel);
			configuraLabels();
			configuraPainel();
			add(painel);
			pack();
		}
		
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		Object source = e.getSource();
		
		if(source == barravolume){
			if (!barravolume.getValueIsAdjusting()){
				eventos.setVolume(barravolume.getValue());
			}
		}	
	}


	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		int p;
		p = contador.getAtualSegundos(); // Pega o total de segundos atual
		progresso.setValue(p); // Atualiza na barra de progressos
		progresso.setString(contador.getSTempoAtual()); // Atualiza a string do tempo na barra de progressos
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		Object source = e.getSource();
		
		if (source == progresso){
			// Pega a posição do mouse
		       int mouseX = e.getX();

		       // Calcula aonde o mouse está na barra de progressos e multiplica pelo valor máximo
		       int progressBarVal = (int)Math.round(((double)mouseX / (double)progresso.getWidth()) * progresso.getMaximum());
		       contador.setTempo(progressBarVal); // Coloca o tempo no contador
		       eventos.setPosicao((long) (progressBarVal*1000000));
		       progresso.setValue(progressBarVal);
		       progresso.setString(contador.getSTempoAtual());
		       eventos.setAndamento(eventos.getAceleracao()); // Volta a tocar no último andamento
		}
	}


	@Override
	public void mouseEntered(MouseEvent arg0) {
		
	}


	@Override
	public void mouseExited(MouseEvent arg0) {
		
	}


	@Override
	public void mousePressed(MouseEvent arg0) {
		
	}


	@Override
	public void mouseReleased(MouseEvent arg0) {
		
	}

}
