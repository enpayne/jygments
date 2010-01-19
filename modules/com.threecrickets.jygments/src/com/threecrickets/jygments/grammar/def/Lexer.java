/**
 * Copyright 2010 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of a BSD license. See
 * attached license.txt.
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.jygments.grammar.def;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.threecrickets.jygments.Filter;
import com.threecrickets.jygments.Jygments;
import com.threecrickets.jygments.ResolutionException;
import com.threecrickets.jygments.Util;
import com.threecrickets.jygments.grammar.Grammar;
import com.threecrickets.jygments.grammar.Token;

/**
 * @author Tal Liron
 */
public class Lexer extends Grammar
{
	//
	// Static operations
	//

	public static Lexer getByName( String name ) throws ResolutionException
	{
		if( Character.isLowerCase( name.charAt( 0 ) ) )
			name = Character.toUpperCase( name.charAt( 0 ) ) + name.substring( 1 ) + "Lexer";

		Lexer lexer = getByFullName( name );
		if( lexer != null )
			return lexer;
		else
		{
			// Try contrib package
			String pack = Jygments.class.getPackage().getName() + ".contrib";
			lexer = getByFullName( pack + "." + name );
			if( lexer == null )
			{
				// Try this package
				pack = Lexer.class.getPackage().getName();
				lexer = getByFullName( pack + "." + name );
			}
			return lexer;
		}
	}

	@SuppressWarnings("unchecked")
	public static Lexer getByFullName( String fullName ) throws ResolutionException
	{
		try
		{
			return (Lexer) Jygments.class.getClassLoader().loadClass( fullName ).newInstance();
		}
		catch( InstantiationException x )
		{
		}
		catch( IllegalAccessException x )
		{
		}
		catch( ClassNotFoundException x )
		{
		}

		InputStream stream = Jygments.class.getClassLoader().getResourceAsStream( fullName.replace( '.', '/' ) + ".json" );
		if( stream != null )
		{
			try
			{
				String converted = Util.rejsonToJson( stream );
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.getJsonFactory().configure( JsonParser.Feature.ALLOW_COMMENTS, true );
				Map<String, Object> json = objectMapper.readValue( converted, HashMap.class );
				Object className = json.get( "class" );
				if( className == null )
					className = "";

				Lexer lexer = getByName( className.toString() );
				lexer.addJson( json );
				lexer.resolve();
				return lexer;
			}
			catch( JsonParseException x )
			{
				throw new ResolutionException( x );
			}
			catch( JsonMappingException x )
			{
				throw new ResolutionException( x );
			}
			catch( IOException x )
			{
				throw new ResolutionException( x );
			}
		}

		return null;
	}

	//
	// Construction
	//

	public Lexer()
	{
		this( false, false, 4, "utf8" );
	}

	public Lexer( boolean stripNewlines, boolean stripAll, int tabSize, String encoding )
	{
		this.stripNewLines = stripNewlines;
		this.stripAll = stripAll;
		this.tabSize = tabSize;
	}

	//
	// Attributes
	//

	public List<Filter> getFilters()
	{
		return filters;
	}

	public boolean isStripNewLines()
	{
		return stripNewLines;
	}

	public void setStripNewLines( boolean stripNewLines )
	{
		this.stripNewLines = stripNewLines;
	}

	public boolean isStripAll()
	{
		return stripAll;
	}

	public void setStripAll( boolean stripAll )
	{
		this.stripAll = stripAll;
	}

	public int getTabSize()
	{
		return tabSize;
	}

	public void setTabSize( int tabSize )
	{
		this.tabSize = tabSize;
	}

	public void addFilter( Filter filter )
	{
		filters.add( filter );
	}

	public float analyzeText( String text )
	{
		return 0;
	}

	public Iterable<Token> getTokens( String text )
	{
		return getTokens( text, false );
	}

	public Iterable<Token> getTokens( String text, boolean unfiltered )
	{
		// text = text.replace( "\r\n", "\n" ).replace( "\r", "\n" );
		// if( stripAll )
		// text = text.trim();
		// if( stripNewLines )
		// text = text.replace( "\n", "" );
		if( tabSize > 0 )
		{
			// expand tabs
		}
		if( !text.endsWith( "\n" ) )
			text += "\n";
		Iterable<Token> tokens = getTokensUnprocessed( text );
		if( !unfiltered )
		{
			// apply filters
		}
		return tokens;
	}

	public Iterable<Token> getTokensUnprocessed( String text )
	{
		return null;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected void addAlias( String alias )
	{
		aliases.add( alias );
	}

	protected void addFilename( String filename )
	{
		filenames.add( filename );
	}

	protected void addMimeType( String mimeType )
	{
		mimeTypes.add( mimeType );
	}

	protected void include( String stateName, String includedStateName )
	{
		getState( stateName ).addDef( new IncludeDef( stateName, includedStateName ) );
	}

	protected void bygroups( String stateName, String pattern, Iterable<String> tokenTypeNames )
	{

	}

	protected void bygroups( String stateName, String pattern, Iterable<String> tokenTypeNames, String nextStateName )
	{

	}

	protected void rule( String stateName, String pattern, String tokenTypeName )
	{
		getState( stateName ).addDef( new TokenRuleDef( stateName, pattern, tokenTypeName ) );
	}

	protected void rule( String stateName, String pattern, String tokenTypeName, String nextStateName )
	{
		getState( stateName ).addDef( new ChangeStateTokenRuleDef( stateName, pattern, new String[]
		{
			tokenTypeName
		}, nextStateName ) );
	}

	protected void rule( String stateName, String pattern, String[] tokenTypeNames )
	{
		getState( stateName ).addDef( new TokenRuleDef( stateName, pattern, tokenTypeNames ) );
	}

	protected void rule( String stateName, String pattern, String[] tokenTypeNames, String... nextStateNames )
	{
		getState( stateName ).addDef( new ChangeStateTokenRuleDef( stateName, pattern, tokenTypeNames, nextStateNames ) );
	}

	protected void addJson( Map<String, Object> json ) throws ResolutionException
	{
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final List<Filter> filters = new ArrayList<Filter>();

	private boolean stripNewLines;

	private boolean stripAll;

	private int tabSize;

	private final List<String> aliases = new ArrayList<String>();

	private final List<String> filenames = new ArrayList<String>();

	private final List<String> mimeTypes = new ArrayList<String>();
}