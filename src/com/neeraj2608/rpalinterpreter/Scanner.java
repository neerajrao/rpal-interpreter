package com.neeraj2608.rpalinterpreter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class Scanner{
  private BufferedReader buffer;
  private String extraCharRead;
  
  public Scanner(String inputFile) throws IOException{
    buffer = new BufferedReader(new InputStreamReader(new FileInputStream(new File(inputFile))));
  }
  
  /**
   * Returns next token from input file
   * @return null if the file has ended
   */
  public Token readNextToken(){
    Token nextToken = null;
    String nextChar;
    if(extraCharRead!=null){
      nextChar = extraCharRead;
      extraCharRead = null;
    } else
      nextChar = readNextChar();
    if(nextChar!=null)
      nextToken = buildToken(nextChar);
    return nextToken;
  }

  private String readNextChar(){
    String nextChar = null;
    try{
      int c = buffer.read();
      if(c!=-1){
        nextChar = Character.toString((char)c);
      } else
          buffer.close();
    }catch(IOException e){
    }
    return nextChar;
  }

  /**
   * Builds next token from input
   * @param currentChar character currently being processed 
   * @return token that was built
   */
  private Token buildToken(String currentChar){
    Token nextToken = null;
    if(LexicalPatterns.LetterPattern.matcher(currentChar).matches()){
      nextToken = buildIdentifierToken(currentChar);
    }
    else if(LexicalPatterns.DigitPattern.matcher(currentChar).matches()){
      nextToken = buildIntegerToken(currentChar);
    }
    else if(LexicalPatterns.OpSymbolPattern.matcher(currentChar).matches()){ //comment tokens are also entered from here
      nextToken = buildOperatorToken(currentChar);
    }
    else if(currentChar.equals("\'")){
      nextToken = buildStringToken(currentChar);
    }
    else if(LexicalPatterns.SpacePattern.matcher(currentChar).matches()){
      nextToken = buildSpaceToken(currentChar);
    }
    else if(LexicalPatterns.PunctuationPattern.matcher(currentChar).matches()){
      nextToken = buildPunctuationPattern(currentChar);
    }
    return nextToken;
  }

  /**
   * Builds Identifier token.
   * Identifier -> Letter (Letter | Digit | '_')*
   * @param currentChar character currently being processed 
   * @return token that was built
   */
  private Token buildIdentifierToken(String currentChar){
    Token identifierToken = new Token();
    identifierToken.setType(TokenType.IDENTIFIER);
    StringBuilder sBuilder = new StringBuilder(currentChar);
    
    String nextChar = readNextChar();
    while(nextChar!=null){ //null indicates the file ended
      if(LexicalPatterns.IdentifierPattern.matcher(nextChar).matches()){
        sBuilder.append(nextChar);
        nextChar = readNextChar();
      }
      else{
        extraCharRead = nextChar;
        break;
      }
    }
    
    identifierToken.setValue(sBuilder.toString());
    return identifierToken;
  }

  /**
   * Builds integer token.
   * Integer -> Digit+
   * @param currentChar character currently being processed 
   * @return token that was built
   */
  private Token buildIntegerToken(String currentChar){
    Token integerToken = new Token();
    integerToken.setType(TokenType.INTEGER);
    StringBuilder sBuilder = new StringBuilder(currentChar);
    
    String nextChar = readNextChar();
    while(nextChar!=null){ //null indicates the file ended
      if(LexicalPatterns.DigitPattern.matcher(nextChar).matches()){
        sBuilder.append(nextChar);
        nextChar = readNextChar();
      }
      else{
        extraCharRead = nextChar;
        break;
      }
    }
    
    integerToken.setValue(sBuilder.toString());
    return integerToken;
  }

  /**
   * Builds operator token.
   * Operator -> Operator_symbol+
   * @param currentChar character currently being processed 
   * @return token that was built
   */
  private Token buildOperatorToken(String currentChar){
    Token opSymbolToken = new Token();
    opSymbolToken.setType(TokenType.OPERATOR);
    StringBuilder sBuilder = new StringBuilder(currentChar);
    
    String nextChar = readNextChar();
    
    if(currentChar.equals("/") && nextChar.equals("/"))
      return buildCommentToken(currentChar+nextChar);
    
    while(nextChar!=null){ //null indicates the file ended
      if(LexicalPatterns.OpSymbolPattern.matcher(nextChar).matches()){
        sBuilder.append(nextChar);
        nextChar = readNextChar();
      }
      else{
        extraCharRead = nextChar;
        break;
      }
    }
    
    opSymbolToken.setValue(sBuilder.toString());
    return opSymbolToken;
  }

  /**
   * Builds string token.
   * String -> ���� (�\� �t� | �\� �n� | �\� �\� | �\� ���� |�(� | �)� | �;� | �,� |�� |Letter | Digit | Operator_symbol )* ����
   * @param currentChar character currently being processed 
   * @return token that was built
   */
  private Token buildStringToken(String currentChar){
    Token opSymbolToken = new Token();
    opSymbolToken.setType(TokenType.STRING);
    StringBuilder sBuilder = new StringBuilder(currentChar);
    
    String nextChar = readNextChar();
    while(nextChar!=null){ //null indicates the file ended
      if(nextChar.equals("\\")){ //match \t, \n, \\, \'
        sBuilder.append(nextChar);
        nextChar = readNextChar();
        if(nextChar!= null && (nextChar.equals("n")||nextChar.equals("t")||nextChar.equals("\\")||nextChar.equals("'"))){
          sBuilder.append(nextChar);
          nextChar = readNextChar();
        }
        else //this represents an error condition and hence we're not concerned with setting extraCharRead
          break;
      }
      else if(LexicalPatterns.StringPattern.matcher(nextChar).matches()){ //match Letter | Digit | Operator_symbol
        sBuilder.append(nextChar);
        nextChar = readNextChar();
      }
      else if(nextChar.equals("\'")){ //we just used up the last char we read, hence no need to set extraCharRead
        sBuilder.append(nextChar);
        opSymbolToken.setValue(sBuilder.toString());
        return opSymbolToken;
      }
    }
    
    return null;
  }
  
  private Token buildSpaceToken(String currentChar){
    Token deleteToken = new Token();
    deleteToken.setType(TokenType.DELETE);
    StringBuilder sBuilder = new StringBuilder(currentChar);
    
    String nextChar = readNextChar();
    while(nextChar!=null){ //null indicates the file ended
      if(LexicalPatterns.SpacePattern.matcher(nextChar).matches()){
        sBuilder.append(nextChar);
        nextChar = readNextChar();
      }
      else{
        extraCharRead = nextChar;
        break;
      }
    }
    
    deleteToken.setValue(sBuilder.toString());
    return deleteToken;
  }
  
  private Token buildCommentToken(String currentChar){
    Token deleteToken = new Token();
    deleteToken.setType(TokenType.DELETE);
    StringBuilder sBuilder = new StringBuilder(currentChar);
    
    String nextChar = readNextChar();
    while(nextChar!=null){ //null indicates the file ended
      if(LexicalPatterns.CommentPattern.matcher(nextChar).matches()){
        sBuilder.append(nextChar);
        nextChar = readNextChar();
      }
      else if(nextChar.equals("\n"))
        break;
    }
    
    deleteToken.setValue(sBuilder.toString());
    return deleteToken;
  }

  private Token buildPunctuationPattern(String currentChar){
    Token deleteToken = new Token();
    deleteToken.setValue(currentChar);
    if(currentChar.equals("("))
      deleteToken.setType(TokenType.L_PAREN);
    else if(currentChar.equals(")"))
      deleteToken.setType(TokenType.R_PAREN);
    else if(currentChar.equals(";"))
      deleteToken.setType(TokenType.SEMICOLON);
    else if(currentChar.equals(","))
      deleteToken.setType(TokenType.COMMA);
    
    return deleteToken;
  }
}
