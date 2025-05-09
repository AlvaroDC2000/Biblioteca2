package models;

import jakarta.persistence.*;

@Entity
@Table(name = "opiniones")
public class Opinion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
    
    private String autor; 
    private String contenido;
    private int nota;

    @ManyToOne
    @JoinColumn(name = "libro_id")
    private Libro libro;

    public Opinion() {}

    public Opinion(String autor, String contenido, int nota, Libro libro) {
        this.autor = autor;
        this.contenido = contenido;
        this.nota = nota;
        this.libro = libro;
    }

    public int getId() { 
      return id; 
      }
    
    public String getAutor() { 
      return autor;
      }
    
    public void setAutor(String autor) { 
      this.autor = autor;
      }

    public String getContenido() { 
      return contenido; 
      }
    
    public void setContenido(String contenido) { 
      this.contenido = contenido;
      }

    public int getNota() { 
      return nota; 
      }
    
    public void setNota(int nota) { 
      this.nota = nota;
      }

    public Libro getLibro() { 
      return libro;
      }
    
    public void setLibro(Libro libro) { 
      this.libro = libro;
      }
    
    public Usuario getUsuario() {
      return usuario;
  }

    public void setUsuario(Usuario usuario) {
      this.usuario = usuario;
  }
}
