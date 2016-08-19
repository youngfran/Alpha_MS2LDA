import numpy as np
import pandas as pd
import csv
from formula import Formula

class Word(object):
    def __init__(self,word_type,word_mass,word_annotation = None):
    	self.word_type = word_type
    	self.word_mass = word_mass
    	self.word_annotation = word_annotation
	

    def __str__(self):
    	line = self.word_type + " " + str(self.word_mass)
    	if not self.word_annotation is None:
    		line += " (" + self.word_annotation + ")"
    	return line



class Topic(object):
	def __init__(self,annotation = None):
		self.words = []
		self.annotation = annotation
	def __iter__(self):
		return iter(self.words)

	def __str__(self):
		line = self.annotation + ": "
		for word in self.words:
			line += "" + str(word) + ","
		return line
		
class Annotation (object):
	"""
		class to hold all the  annotated topics and their words from one or more CSV files
		anno is a list of topics from above
	"""	
	
	def __init__(self,anno_files, labels = ['']):
	
		self.topics =[]
			
		for i,f in enumerate(anno_files):
			inFile = open(f)
			inReader = csv.reader(inFile)
			label = labels[i]
			for row in inReader:
				if inReader.line_num == 1: # skip header can change 
					continue    

				if   row[0] is not "":
					new_topic = Topic(annotation = label+row[4])
					self.topics.append(new_topic)
					words =[]
					
				word_annotation = str.strip(row[3],',')
				f = Formula(word_annotation)
				mass = f.compute_exact_mass()
				if mass == 0:
					mass = float(str.strip(row[2],','))
				new_word = Word(str.strip(row[1],','),mass,word_annotation )
				
				new_topic.words.append(new_word)
		
	
		
	def __str__(self):
		s = ""
		for topic in self.topics:
			s = s + " "  + str(topic) + "\n"
		return s
		
	def __iter__(self):
		return iter(self.topics)
		
	def __next__(self):
		fib = self.a
		if fib > self.max:
			raise StopIteration
		self.a, self.b = self.b, self.a + self.b
		return fib
	
		
	

	def get_hits (self,model,topics=[],ppm_tolerance = 20, word_threshold = 0.001,score_threshold = 0.3):
	# returns a list of
		
	# if topics is an empty list then find hits for all topics in LDA model	
		if  not topics  :
			topics = range(model.K)
		
		topic_hits = {}
		for k in topics:
			words = self.get_words(model,k,ppm_tolerance=ppm_tolerance,word_threshold = word_threshold )
			hits = self.find_topic_hits (words,score_threshold )
			words_out = self.get_words(model,k,ppm_tolerance,word_threshold = 0.01 )
			topic_hits[k] = {'model_words' : words_out,
							  'hits' : hits}

		return topic_hits
		
	
	
	def  get_words(self,model,k, ppm_tolerance = 20, word_threshold = 0.001 ):   
		topic = 'motif_{}'.format(k) 
		model_words = []
		word_dict = model.topic_dict[topic]
		words = [(word,word_dict[word]) for word in word_dict]
		words = sorted(words,key = lambda x: x[1],reverse = True)	
		for word,prob in words:
			if prob > word_threshold:
				if word.startswith('loss'): 
					ppm = 2*ppm_tolerance
					w_type = 'Loss'
				else:
					ppm = ppm_tolerance
					w_type = 'Frag'
				mass = float(word.split('_')[1])
				
				model_words.append((ppm,mass,prob,w_type))
		
		return (model_words)
	

	def find_topic_hits (self,model_words,score_threshold):
	
	# finds a list of annotation topics where a words from LDA topic and annotation topic match within mass tolerance and word type.
	# return a list of annotation topics and their scores.
		# Calculate the mass range for each word within tolerance
		# (work around to stop numerical values being converted to strings)
		ppm,mass,probability,w_type =map(list,zip(*model_words))
		
		mass = np.array( mass)
		ppm = np.array(ppm)
		tolerance = mass * ppm * 1e-6
		mass_range = np.transpose((mass + tolerance, mass - tolerance))
				
		# list comprehension for each word in LDA_topic find matches in annotation topics. 		
		all_hits = []
		for w,m in enumerate(mass_range):
			hits =[(topic_anno, w)
				for topic_anno in self.topics
					for word in topic_anno.words
						if ((word.word_type == w_type[w]) and
							(word.word_mass <= m[0])and
							(word.word_mass >= m[1]))]			
			all_hits.extend(hits)				
		
		# Score each occurance of an annotation topic in all_hits .
		#Score =  Sum of the probabilities of matching  words
		# Keep track of number of matching words and their position
		hit_dict = {} 
		for hit,w in all_hits:	
			if hit in hit_dict:
				hit_dict[hit] ['score'] += probability[w] 
				hit_dict[hit]['count'] += 1
				hit_dict[hit]['words'].append(w) 	
			else:
				hit_info = {'score' : probability[w],
								'count' : 1,
								'words' : [w] }				 
				hit_dict[hit] = hit_info
							
		# Only keep hits that are above the score_threshold
		#{k:v for (k,v) in dict.items() if v > something}
		passed_hits={h:h_v for (h,h_v) in hit_dict.items() if h_v['score'] > score_threshold}

		return passed_hits
		
	def display_hits (self,model,interesting):
		all_hits = self.get_hits(model,topics = interesting)

		for k,h in all_hits.items():   
			if len(h['hits']) > 0:  
				m_w  = ['{}_{}'.format(t,m) for (ppm,m,p,t) in h['model_words'] if p > 0.01]    
       
				print  "\n\n"," Topic " , k,  " hits" ,len(h['hits']),'\n Model Words:', m_w
				for an,a in h['hits'].items():
					print  "\n Score:",a['score'],'number of words matching',a['count'], a['words']
					print  an
			else:
				print k, '...No hits'
		
	def write_csv (self,model, output_file):
	
		all_hits = self.get_hits(model)
		with open(output_file, 'w') as csvfile:
			fieldnames = ['lda_topic','topic_hits','lda_word','lda_word_prob','hit','word_hit_count',"anno_score","topic_anno",
                   'anno_w_type',  'anno_w_mass','anno_w_anno']
			writer = csv.DictWriter(csvfile,delimiter=',', lineterminator='\n',  fieldnames=fieldnames)
			writer.writeheader()
			total_hits = 0
			topic_count = 0
			for topic,h in all_hits.items():
				n_hits = len(h['hits'])
				total_hits += n_hits
				model_w  = [(('{}_{}'.format(t,m)),p) for (ppm,m,p,t) in h['model_words'] if p > 0.01] 
				if n_hits > 0:
					for an,a in h['hits'].items():
						writer.writerow({'lda_topic': topic, 'topic_hits':n_hits,'word_hit_count': a['count'] ,'anno_score': a['score'],'topic_anno': an.annotation})
						hit_index = a['words']
						if n_hits != 0:
							for i in range (max (len(h['model_words']),len (an.words))):
								hit = ''
								if (i <  len(h['model_words'])) :
									w,prob = model_w[i]
									if i in hit_index:
										hit = 'hit'
								else: 
									w =''
									prob =''
								if i< len (an.words):             
									a = an.words[i]
								else:
									a = Word("","","")

								writer.writerow({'lda_word': w,  'lda_word_prob': prob, 'hit':hit,
									'anno_w_type':a.word_type, 'anno_w_mass': a.word_mass,'anno_w_anno':a.word_annotation})
   
				else:
					writer.writerow({'lda_topic': topic, 'topic_hits':n_hits})
					for i in range (len(h['model_words'])):
						w,prob = model_w[i]
						writer.writerow({'lda_word': w,  'lda_word_prob': prob})
							