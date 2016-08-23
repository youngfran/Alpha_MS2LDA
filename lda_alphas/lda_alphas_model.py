import numpy as np
import pandas as pd
import pickle
from sklearn.decomposition import PCA
import plotly
plotly.offline.init_notebook_mode()
from plotly.graph_objs import *

from multifile_analysis import MultifileAnalysis
		
class TopicModel(object):
	"""Class to cotain all the varibles and methods related to the topics of a multifile LDA model
		attributes :
					K - the number of topics 
					F - the number of Files/samples
					alphas - array of F x K 
					topic_dict - dictionaray key is the topic , value is a dictionary of words
					topic_index - index of the order of topics in the array
					sampleIDs - passed in for Joe's model 
					groups - [[group1,group2]] list of booleans length F . true indicates sample in group
					groupIDs - [[group1Ids],[group2Ids]] for plotting
						 both set with set_groups
					clusters -to contain a list length K , to indicate which cluster a topic is in 
							set with set_cluster(clusters)
	"""
 
	def __init__(self,fileIn, model_type = "nb", sampleIDs = [], alpha_exp = True):
 
		if model_type == 'gs':   # Gibb Sampling  Joe's model
			analysis = MultifileAnalysis.resume_from('results/13_files_separate_mode_method1_POS.project')
			print analysis.last_saved_timestamp, analysis.message
			self.K = analysis.K
			self.F = analysis.F
			self.sampleIDs = sampleIDs
			self.alphas = np.array(analysis.model.mean_alpha)
			self.topic_dict = self.make_topic_dict( self,analysis.model)
			self.topic_index = self.make_topic_index(self,self.K)
	
	
		else:  # model_type  Simon's model
				# Assumption made that topic names are of form 'motif_k' 
				#and alphas are in order of k = 1 to K  need to change
			with open(fileIn,'r') as f:
				model = pickle.load(f)
			self.sampleIDs = []
			# The individual LDA objects are accessed through the 'individual_lda' key
			self.F =  len(model['individual_lda'])
			self.K =  model['K']
			first_lda = model['individual_lda'].keys()[10]
			self.topic_dict = model['individual_lda'][first_lda]['beta']
			first_lda = model['individual_lda'].keys()[0] 
			self.topic_index = model['individual_lda'][first_lda]['topic_index']
			# load all the alphas into an array
			self.alphas = np.zeros((self.F,self.K))   
			for i,file_name in enumerate(model['individual_lda']):
				self.sampleIDs.append(file_name) # keep track of these in the same order as the alphas
				alpha_list = model['individual_lda'][file_name]['alpha']
				self.alphas[i,:] = np.array(alpha_list).copy() # copy in case we change it by accident	

		
		# if alpha_exp parameter set use expected alphas
		if alpha_exp:
			self.set_expected_alpha()
		
					
		# initialise instance variables not set
		self.clusters = []
		self.names = ['','']  
		self.set_groups()
		




	def get_topic_index(self,topic):
		return self.topic_index[topic] 
		
	def get_topic_name (self,index):
		topic_pos = [(topic,topic_index[topic]) for topic in topic_index]
		topic_pos = sorted(topic_pos,key = lambda x: x[1]) # sort this by position
		reverse,_ = zip(*topic_pos) # extract the names
		# reverse holds the names of the topics in the order they appear in alpha
		return reverse[index]	
		
	def set_sampleIDs(self,sampleIDs):
		self.sampleIDs = sampleIDs
		self.set_groups()
	def set_clusters(self,clusters):
		self.clusters = clusters
		
	def set_names (self, names):
		self.names = names
		
	def set_groups(self,groupings = [], names = []):
		# groups to contain two lists of booleans for group1 and group2 each length F 
		# groupIDs two lists with appropriate sampleIDs
		if names:  # Use names to set groups
			self.names = names
			group1=[id.startswith(names[0]) for id in self.sampleIDs ]
			group2 = [not g for g in group1]  
			self.groups = [group1,group2]
			
			# Get a list of the id's for each group (used in plotting)
			group1ID = [s for i,s in enumerate (self.sampleIDs) if group1[i]]
			group2ID = [s for i,s in enumerate (self.sampleIDs) if group2[i]]
			self.groupIDs = [group1ID,group2ID]
		
		elif  len(groupings) != 0:       # Use  to set grouping list from metadata
			self.names = ['group1','group2']
			d = pd.Series (groupings,dtype = "category")
			group1 = np.array(d == 1)
			group2 = np.array(d == 0)
			self.groups = [group1,group2]
			# Get a list of the id's for each group (used in plotting)
			group1ID = [s for i,s in enumerate (self.sampleIDs) if group1[i]]
			group2ID = [s for i,s in enumerate (self.sampleIDs) if group2[i]]
			self.groupIDs = [group1ID,group2ID]
			
			
			
		else: 
			# initialise  groups so everything in one group 
			# to contain two lists of booleans for group1 and group2 each length F 
			g1 = [True]*self.F
			g2 = [False]*self.F
			self.groups = [g1,g2]
			self.groupIDs = [self.sampleIDs,[]]
			
			
	def make_topic_dict (self , model, word_threshold=0.001):
		
		topic_dict = {}
		for k in range(self.K):
			topic =  "motif_{}".format(k)
			words={}
			for i in range(len(model.vocab)):
				w = model.vocab[i]
				probability  = model.topic_word_[k, i]
				if probability > word_threshold :
					words[w] = probability
			topic_dict[topic] = words
		return topic_dict
		
	def make_topic_index (self, K):
		return { 'motif_{}'.format(k):k for k in range(K)}
		
	def set_expected_alpha(self):
		""" Normalise the alphas across the samples """
		return  np.divide(self.alphas, self.alphas.sum(axis = 1)[:,None])
	
	
	def sort_alphas(self):
		""" method to sort the array of alphas so all the rows (samples) from the same group are together
			Sorts self.alphas and updates the sampleIDs and groups to the new order. 
		"""
		groups = self.groups
		if  not groups : # no grouping information on which to sort
			print 'No grouping information - array not sorted, set_groups first'
			return
		
		# Create a sort index so all samples from each group are together
		# Assumption is some samples may not belong to a group (eg pooled samples)
		sort_index = []
		for i in range(self.F):
			if groups[0][i]:
				sort_index.append(i)
		for i in range(self.F):
			if groups[1][i]:
				sort_index.append(i)

		# update sampleIDs into IDs sorted by group
		self.sampleIDs = np.array(self.sampleIDs)[sort_index]
		#update alphas to the same order
		self.alphas = self.alphas[sort_index,:]
		
		# update the groups to new sorted order
		#group1 = self.groups[0]
		group1 = np.array(self.groups[0])[sort_index]
		group2 = [not g for g in group1]  
		self.groups = [group1,group2]
		
		# update groupIDs
		group1ID = [s for i,s in enumerate (self.sampleIDs) if group1[i]]
		group2ID = [s for i,s in enumerate (self.sampleIDs) if group2[i]]
		self.groupIDs = [group1ID,group2ID]

	 

		
	def  get_topic_text(self,total_prob_threshold =0.95):   
		""" Meyhod to consruct a dictionary of the topic annotations used in plotPCA
		"""
		topic_text= {}
 		for topic in self.topic_dict:
    
			word_dict = self.topic_dict[topic]
			words = [(word,word_dict[word]) for word in word_dict]
			words = sorted(words,key = lambda x: x[1],reverse = True)
    
			prob_thresh = total_prob_threshold
			motif_string = "\n{} : ".format (topic)
			total_prob = 0.0
			word_string = ''
			for word,prob in words:
				word_string += "{} : {:.3f}\n".format(word,prob)
				total_prob += prob
				if total_prob > prob_thresh or prob <0.01  :
					break
			motif_string += '{}\n{}'.format(total_prob,word_string)           
			k= int(str.split(topic,'_')[1])
			topic_text[k]= motif_string
		
		return topic_text
		
	@classmethod
	def load_alphas(cls, file_in):
		with open(file_in, 'rb') as fp:
			obj = pickle.load( fp)
			return obj

	def save_alphas(self, file_out):  
		with open(file_out, 'wb') as fp:
			pickle.dump(self, fp)
            
    
    
	def plot_PCA(self, title = ''):
		""" Method to plot an interactive plotly PCA of the Alphas
			and include information in the object such
 			as groupings of the sample and clustering of the topics
			These need to be set first otherwise treated as 1 group and 1 cluster
		"""
		
		# perform PCA on the alphas
		pca = PCA(n_components = 2,whiten = True)
		pca.fit(self.alphas)
		X = pca.transform(self.alphas)
		loadings = pca.components_
		if not self.groupIDs[0] :	
			self.groupIDs[0] = self.sampleIDs
		
		
		# set clusters
		if   self.clusters == [] :
			self.clusters =[0]*self.K
			colours = ['rgba(10,200,200,0.3)']# colour all the same
		else:
			N = max(self.clusters) +1
			colours = ['hsl('+str(h)+',50%'+',50%)' for h in np.linspace(0, 360, N)]
		# Get the text (words) to annotate topics in hovermode
		topic_text = self.get_topic_text()
		
		#Create Plotly Plot 
		traces = []
		for i,group in enumerate(self.groups):
			sample = Scatter(
				x = X[np.array(group),0],
				y = X[np.array(group),1],
				mode = 'markers',
				text = self.groupIDs[i],
				name= self.names[i],
				marker=Marker(
					size=8,
					line = Line(color='rgba(100,100, 100, 0.4)',
							width=0.5),
					opacity=0.5)
				)
			traces.append(sample)

		xs =[]
		ys =[]

		for i in range (self.K): 
			clust = self.clusters[i]
			c = colours[clust]
    
			xs = [0,4*loadings[0,i]]
			ys = [0,4*loadings[1, i]]
			load = Scatter(
				x =  xs,
				y =  ys ,
				mode = 'lines',
				#text = tfs[i],
				text = topic_text[i],
				showlegend = False,
				line = dict(color= c)
				#line = dict(color='rgba(100,100,250,0.3)')
			)
			traces.append(load)    
		
		data = Data (traces)
		layout = Layout(title = title,
                hovermode= 'closest',
                showlegend=True,
                yaxis = dict(title= 'PCA2',showline = False),
                xaxis = dict(title= 'PCA1',showline = False)
             )
		fig = Figure(data= data, layout= layout)       
		plotly.offline.iplot(fig)
		
	def topic_barplots(self,interesting,names = [] , title = ''):
		
		data = []
		
		for i, sample  in  enumerate(self.sampleIDs):
			if self.groups[0][i]:
				trace1 = Bar(
					x = interesting,
					y = self.alphas[i, interesting],
					name= self.names[0],
					marker=dict(
						color='rgba(250,0,128,0.5)' )
				)
				data.append(trace1)
		for i, sample  in  enumerate(self.sampleIDs):
			if self.groups[1][i]:
				trace2 = Bar(
					x = interesting,
					y = self.alphas[i, interesting],
					name= self.names[1],
					marker=dict(
						color='rgba(214,12,140,50)' )
				)
				data.append(trace2)


		layout = Layout(
						title= title,
						xaxis=dict(
							title='Alpha',
							tickfont=dict(
								size=14,
								color='rgb(107, 107, 107)' )
						 ),
						yaxis=dict(
							title='Alpha',
							titlefont=dict(
								size=16,
								color='rgb(107, 107, 107)'
							),
							tickfont=dict(
								size=14,
								color='rgb(107, 107, 107)'
							)
						),
						legend=dict(
								x=0,
								y=1.0,
								bgcolor='rgba(255, 255, 255, 0)',
								bordercolor='rgba(255, 255, 255, 0)'
						),
						barmode='group',
						bargap=0.15,
						bargroupgap=0.1
					)

		fig = Figure(data=data, layout=layout)
		plotly.offline.iplot(fig)
	
	def topic_boxplots(self,interesting,group_names = [] , anno = [], title = ''):
		""" Method to plotly Boxplot for the topics in interesting (a list of Int)
		Anno is a associted topic annotation fie.putative sub-structure
		"""
		
		alphas = self.alphas
		groups = self.groups
		
		if  group_names : # If no group names are passed in use object group names
			names = group_names
		else:	
			names = self.names
			
		
		gp = [names[0] if g else names[1] for g in groups[0]]
		# rearrange data so plots grouped by topic
		if anno:
			x_g1 = sum( [[a]*sum(groups[0]) for a in anno],[])
			x_g2 = sum( [[a]*sum(groups[1]) for a in anno],[])
		else:
			x_g1 = sum( [["M_{}".format(t)]*sum(groups[0]) for t in interesting],[])
			x_g2 = sum( [["M_{}".format(t)]*sum(groups[1]) for t in interesting],[])
		
		# gp  converts boolean list of groups to string in names to avoid using boolean
		# to pull out correct alphas
		gp = [names[0] if g else names[1] for g in groups[0]]
		y_g1 = []
		y_g2 = []
		for t in interesting:
			y_g1.extend(list(alphas[gp == np.array(names[0]),t]))
			y_g2.extend(list(alphas[gp == np.array(names[1]),t]))
		
		# plot the data			
		trace0 = Box(
					y = y_g1, 
					x = x_g1,
					name = names[0],
					marker = dict(color = 'rgb(0,128,128)')
					) 
		trace1 = Box(
					y = y_g2, 
					x = x_g2,
					name = names[1],
					marker = dict(color = 'rgb(214,12,140)')
					) 
					
		data = [trace0, trace1]
	
		# format the layout
		layout = {'xaxis': {'showgrid':False,'zeroline':False, 'title':'Motif','tickangle':45},
          'yaxis': {'zeroline':False,'gridcolor':'white','title':'Alpha'},
          'title': title,
		  'paper_bgcolor': 'rgb(233,233,233)',
          'plot_bgcolor': 'rgb(233,233,233)',
		  'boxmode': 'group'
          }

		fig = Figure(data= data, layout= layout)       
		#py.iplot(fig)
		plotly.offline.iplot(fig)


