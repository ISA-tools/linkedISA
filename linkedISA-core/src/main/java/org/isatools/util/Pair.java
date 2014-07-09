package org.isatools.util;

/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 21/02/2013
 * Time: 18:20
 *
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class Pair<A, B>   {
    public A first;
    public B second;

    public Pair(A fst, B snd) {
        first = fst;
        second = snd;
    }

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }

    public String toString(){
        StringBuffer buffer = new StringBuffer();
        buffer.append("Pair(");
        buffer.append(first);
        buffer.append(",");
        buffer.append(second);
        buffer.append(")");
        return buffer.toString();
    }
}