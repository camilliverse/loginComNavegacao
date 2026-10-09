package br.edu.unifaj.cc.mobile.logincomnavegacao.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    
    private static final String FORMATO_DATA = "dd/MM/yyyy";

    public static String formatarData(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat(FORMATO_DATA, new Locale("pt", "BR"));
        return sdf.format(date);
    }
    
    /**
     * Converte "dd/MM/yyyy" em Date, devolvendo null se o texto não for uma
     * data real.
     *
     * O SimpleDateFormat é leniente por padrão e aceitaria "31/02/2025"
     * como 03/03/2025, ou "32/13/2025" como 01/02/2026 - datas que não
     * existem e passariam a valer como se fossem válidas. Aqui a tolerância
     * fica desligada e o texto tem que bater inteiro, senão devolve null.
     */
    public static Date parseData(String dataStr) {
        if (dataStr == null || dataStr.isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(FORMATO_DATA, new Locale("pt", "BR"));
            sdf.setLenient(false);
            return sdf.parse(dataStr);
        } catch (ParseException e) {
            return null;
        }
    }
    
    /**
     * Converte a data ISO do backend ("2025-12-30") para o formato da tela
     * ("30/12/2025"). Texto fora do padrão volta como veio.
     */
    public static String isoParaBr(String iso) {
        if (iso == null || iso.length() < 10) {
            return iso == null ? "" : iso;
        }
        String[] partes = iso.substring(0, 10).split("-");
        if (partes.length != 3) {
            return iso;
        }
        return partes[2] + "/" + partes[1] + "/" + partes[0];
    }

    /**
     * Converte a hora ISO do backend ("09:30:00") para "09:30".
     */
    public static String horaParaBr(String isoHora) {
        if (isoHora == null || isoHora.isEmpty()) {
            return "";
        }
        return isoHora.length() >= 5 ? isoHora.substring(0, 5) : isoHora;
    }

    /**
     * Converte a data da tela ("30/12/2025") para o formato ISO do backend
     * ("2025-12-30"). Texto inválido volta como veio.
     */
    public static String brParaIso(String br) {
        if (br == null || br.length() < 10) {
            return br == null ? "" : br;
        }
        String[] partes = br.split("/");
        if (partes.length != 3) {
            return br;
        }
        return partes[2] + "-" + partes[1] + "-" + partes[0];
    }

    public static String somarDias(String data, int dias) {
        Date d = parseData(data);
        if (d == null) return "";
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.DAY_OF_MONTH, dias);
        
        return formatarData(cal.getTime());
    }
    
    public static String calcularValidadeBolsa(String dataColeta) {
        return somarDias(dataColeta, 42);
    }

    /**
     * Informa se a data (dd/MM/yyyy) é hoje ou já passou.
     *
     * Usado para bloquear o registro de coleta de um agendamento que ainda
     * vai acontecer. Data ilegível devolve false: na dúvida, não registra.
     */
    public static boolean isDataNoPassadoOuHoje(String data) {
        Date alvo = parseData(data);
        if (alvo == null) {
            return false;
        }
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        return !alvo.after(cal.getTime());
    }
}