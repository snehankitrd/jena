/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse;


import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.PrintSerializable;
import com.hp.hpl.jena.sparql.util.PrintUtils;

public class Item extends ItemLocation implements PrintSerializable
{
    protected ItemList list = null ;
    protected Node node = null ;
    protected String word = null ;
    protected boolean isNil = false ;
    
    public static Item createList() { return createList(noLine, noColumn) ; }
    public static Item createList(int line, int column)
    {
        Item item = new Item(line, column) ;
        item.list = new ItemList(line, column) ;
        return item ;
    }
    
    public static Item createList(ItemList list)
    { return createList(list, noLine, noColumn) ; }
    
    public static Item createList(ItemList list, int line, int column)
    {
        Item item = new Item(line, column) ;
        item.list = list ;
        return item ;
    }
    
    public static Item createNode(Node node) { return createNode(node, noLine, noColumn) ; } 
    public static Item createNode(Node node, int line, int column)
    {
        Item item = new Item(line, column) ;
        item.node = node ;
        return item ;
    }
    
    public static Item createWord(String word) { return createWord(word, noLine, noColumn) ; }
    public static Item createWord(String word, int line, int column)
    {
        if ( word.equals("nil") )
            return createNil(line, column) ;
        Item item = new Item(line, column) ;
        item.word = word;
        return item ;
    }
    

    public static Item createNil() { return createNil(noLine, noColumn) ; }
    public static Item createNil(int line, int column)
    { 
        Item item = new Item(noLine, noColumn) ;
        item.isNil = true ;
        return item ;
    }
    
    
    private Item(int line, int column)
    {
        super(line, column) ;
    }
    
    private Item() { super(noLine, noColumn) ; }

    // ---- Equality and hashcode
    
    static class ItemHashCode implements ItemVisitor
    {
        int hashCode = 0 ;
        public void visit(Item item, ItemList list)
        { hashCode = list.hashCode() ; }

        public void visit(Item item, Node node)
        { hashCode = node.hashCode() ; }

        public void visit(Item item, String word)
        { hashCode = word.hashCode() ; }
        
        public void visitNil()
        { hashCode = -99 ; }
    }
    
    public int hashCode()
    {
        ItemHashCode itemHashCode = new ItemHashCode() ;
        this.visit(itemHashCode) ;
        return itemHashCode.hashCode ;
    }
    
    static class ItemEquals implements ItemVisitor
    {
        private Item other ;
        ItemEquals(Item other) { this.other = other ; }
        boolean result = false ;

        public void visit(Item item, ItemList list)
        { result = ( other.isList() && other.getList().equals(list) ) ; } 

        public void visit(Item item, Node node)
        { result = ( other.isNode() && other.getNode().equals(node) ) ; }

        public void visit(Item item, String word)
        { result = ( other.isWord() && other.getWord().equals(word) ) ; }

        public void visitNil()
        { result = other.isNil() ; }

    }
    
    public boolean equals(Object other)
    { 
        if ( ! ( other instanceof Item ) ) return false ;
        
        ItemEquals x = new ItemEquals((Item)other) ;
        this.visit(x) ;
        return x.result ;
    }
    
    // ----

    public ItemList getList()           { return list ; }
    public Node getNode()               { return node ; }
    //public String getPrefixedName()     { return prefixedName ; }
    public String getWord()             { return word ; }
    
    public String sniff()
    {
        if ( ! isTaggable() ) return null ;
        return getList().get(0).getWord() ;
    }
    
    public boolean isTaggedIgnoreCase(String tag)
    {
        if ( ! isTaggable() ) return false ;
        return getList().get(0).isWordIgnoreCase(tag) ;
    }
    public boolean isTagged(String tag)
    {
        if ( ! isTaggable() ) return false ;
        return getList().get(0).isWord(tag) ;
    }
    public boolean isTagged()
    {
        if ( ! isTaggable() ) return false ;
        return list.get(0).isWord() ; 
    }
    private boolean isTaggable()
    {
        if ( ! isList() ) return false ;
        if ( list.size() == 0 ) return false ;
        return true ; 
    }
    
    public boolean isNil()              { return isNil ; } 
    public boolean isList()             { return list != null ; }
    public boolean isNode()             { return node != null ; }
    public boolean isWord()             { return word != null ; }
    public boolean isWord(String testWord)
    { 
        if ( word == null )
            return false ;
        return word.equals(testWord) ;
    }
    public boolean isWordIgnoreCase(String testWord)
    { 
        if ( word == null )
            return false ;
        return word.equalsIgnoreCase(testWord) ;
    }
    public void visit(ItemVisitor visitor)
    {
        if ( isList() )
            visitor.visit(this, getList()) ;
        else if ( isNode() ) 
            visitor.visit(this, getNode()) ;
        else if ( isWord() )
            visitor.visit(this, getWord()) ;
        else if ( isNil() )
            visitor.visitNil() ;
        else
            System.err.println("broken item") ;
    }
    
    public Item transform(ItemTransform transformer)
    {
        // transformations keep the list structure
//        if ( isList() )
//            return transformer.transform(this, getList()) ;
//        else
        if ( isNode() ) 
            return transformer.transform(this, getNode()) ;
        else if ( isWord() )
            return transformer.transform(this, getWord()) ;
        else
            System.err.println("broken item") ;
        return null ;
    }
    
    public String toString(PrefixMapping pmap)
    { return PrintUtils.toString(this, pmap) ; }

    //@Override
    public String toString()
    { return PrintUtils.toString(this) ; }
    
    public void output(IndentedWriter out)
    { output(out, null) ; }
    
    public void output(IndentedWriter out, SerializationContext sCxt)
    {
        ItemWriter.write(out, this, sCxt) ;
    }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */