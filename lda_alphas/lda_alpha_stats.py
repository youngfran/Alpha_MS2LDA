"""Functions for differential analysis of alphas between samples in multifile LDA model
"""
import math
import pandas as pd
import pylab as plt
import seaborn as sns
import numpy as np
from scipy.stats import ttest_ind, mannwhitneyu

def alpha_stats (alphas, groups, significant = 0.05):
	"""
	Method to calculate the statistical significance between 2 groups from the alphas
	Parameters:
		alphas  an array of topics x samples 
		groups  ( for 2 groups only) a list of booleasn group1 == true group2 == False)
	Returns
		a data frame with FC , T-test pvalues, Mann-Whitney p-values 
		and p-adjusted values for each topic
	"""
	
	K = alphas.shape[1]
	group1 = np.array(groups)
	group2 = ~ group1

	#T-test
	a = alphas[group1 ] 
	b = alphas[group2]

	#T-test
	tt,p_tt = ttest_ind (a,b)
	# Mann Whitney Test
	mw = []
	p_mw =[]
	for i in range (K):
		 m,p =mannwhitneyu(a[:,i],b[:,i])
		 mw.append(2*m)
		 p_mw.append(p)
		
	# create a data frame with FC, T , p-value, adjusted p-value for each topic
	rows = []
	K = alphas.shape[1]
	fc =  np.log2(np.mean(a,axis=0)) - np.log2(np.mean(b, axis=0))
	p_adjust_tt = p_adjust_bh(p_tt)
	p_adjust_mw = p_adjust_bh(p_mw)
	
	for i in range(K):
		sig_tt = p_adjust_tt < significant
		sig_mw = p_adjust_mw < significant
		rows.append ((i,fc[i], p_tt[i],p_adjust_tt[i],sig_tt[i],p_mw[i],p_adjust_mw[i],sig_mw[i]))

	df1 = pd.DataFrame(rows, columns=[ 'topic', 'log2FC', 'TT p-value','TT p_adjust', 'TT significant', 'MW p-value','MW p_adjust', 'MW significant'])
	return df1




def p_adjust_bh(p):
    """Benjamini-Hochberg p-value correction for multiple hypothesis testing."""
#	from stack overflow

    p = np.asfarray(p)
    by_descend = p.argsort()[::-1]
    by_orig = by_descend.argsort()
    steps = float(len(p)) / np.arange(len(p), 0, -1)
    q = np.minimum(1, np.minimum.accumulate(steps * p[by_descend]))
    return q[by_orig]

def get_DE (alphas,n =10 ):
	"""
	find the differential expression ( alpha(f1,k) - alpha (f2, k)) from  LDA.mean_alphas 
	returns DE  array of F x F  x K containing compaision between each file for each topic
        max_DEs the n topics showing the biggest value in DE
	"""
	max_DEs = [] 
	n_samples  = alphas.shape[0] 
	n_topics   = alphas.shape[1]
   
	#create an array for all pairwise comparisons of the samples 
	DE = np.zeros(shape=(n_samples,n_samples,n_topics))

	for s1 in range(n_samples -1):
		for s2 in range (s1+1, n_samples):
			a = np.array(alphas[s1,:] - alphas[s2,:])
			DE [s1,s2] = a
	# find the topics with top n DE
			max_DE =[]
			absa = abs(a)
            
			for i in range (n):
				max_pos = absa.argmax()
				absa[max_pos] = -100
				max_DE.append(max_pos) 
			max_DEs.extend(max_DE)
	max_DEs = list(set(max_DEs))

	return(DE,max_DEs)
        
        
        

	
# find the Fold Change ( log2(alpha(f1,k)) - log2(alpha (f2, k))) from  LDA 
# return FC  array of F x F  x K containing compaision between each file for each topic	
def get_FC (alphas,n =10 ):
	"""
	Finds the Fold Change ( log2(alpha(f1,k)) - log2(alpha (f2, k))) from  LDA 
	Return FC  array of F x F  x K containing compaision between each file for each topic
		max_FCs - the n topics with the biggest values of FC
	"""

	log_alpha = np.log2(np.array(alphas))
	FC, max_FCs = get_DE(log_alpha,n)

	return(FC,max_FCs)
	
	

def plot_barplots (alphas,interesting, title = 'Plot of alphas for each sample'):
	"""Plots barplots aof the alphas values for each sample for each topic in interesting
		Problems with legend  and colouring on group
		replaced by topic_barplots and topic_boxplots in lda_topivmodel
	"""
	
	sns.set(style="white", context="talk")
	sns.set_context({"figure.figsize": (20, 5)})    
	no_files = (alphas.shape[0]) # Number of files (samples) in the model
	no_topics = alphas.shape[1]  # Number of topics in the model
	file_ids = []
	topic_ids = []
	alpha = [] 

	for f in range(no_files): 
		
		file_ids.extend([f for k in range(no_topics)])
		topic_ids.extend([k for k in range(no_topics)])
    
		alpha_mean = alphas[f][:]
		assert len(alpha_mean) == no_topics
		alpha.extend(alpha_mean.tolist())
    

	rows = []
	for i in range(len(topic_ids)):
		topic_id = topic_ids[i]
		if topic_id in interesting:
			rows.append((file_ids[i], topic_id, alpha[i]))

	df = pd.DataFrame(rows, columns=['file', 'topic', 'alpha'])
	fig = sns.barplot(x="topic", y="alpha", hue='file', data=df)
    
	fig.figure.suptitle(title, fontsize = 24)
	plt.ylabel('Alpha (probability) ', fontsize=16)
    
	
	
	
	