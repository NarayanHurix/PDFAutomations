package com.hurix.chain.define;

import java.util.List;

import com.hurix.chain.input.ChainRequest;

public interface Chain {
	  public abstract void setEnv(List<String> options);
	  public abstract void setNextInChain(Chain nextInChain);
	  public abstract void process(ChainRequest request);
}
