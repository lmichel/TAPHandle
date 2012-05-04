jQuery.extend({

	CartModel: function(nodekey, description){

		var listeners = new Array();
		var that = this;

		var cartData = {};
		var zipJob;
		/**
		 * add a listener to this view
		 */
		this.addListener = function(list){
			listeners.push(list);
		}
		this.addJobResult = function(nodekey, jobid) {
			var entry;
			if( (entry = cartData[nodekey]) == undefined ) {
				cartData[nodekey] = {jobs: new Array(), urls: new Array()};
				cartData[nodekey].jobs[0] = {name: jobid, uri: jobid};
				logMsg("add " + nodekey + " 0000 " + jobid);
			}
			else {
				var jobs = entry.jobs;
				for( var i=0 ; i<jobs.length ; i++ ) {
					if( jobs[i].uri == jobid ) {
						logged_alert("Result of job " + nodekey + "." + jobid + " already in the cart", "input Error");
						return;
					}
				}
				logMsg("add " + nodekey + " " + i + " " + jobid);
				cartData[nodekey].jobs[i] = {name: jobid, uri: jobid};			
			}
		}
		this.removeJobResult = function(nodekey, jobid) {
			var entry;
			if( (entry = cartData[nodekey]) == undefined ) {
				logged_alert("Ther is no data associated with node " + nodekey + " in the cart", "input Error");
			}
			else {
				var jobs = entry.jobs;
				for( var i=0 ; i<jobs.length ; i++ ) {
					if( jobs[i].uri == jobid ) {
						logMsg("remove job " + nodekey + "." + jobid+ " from the cart");
						jobs.splice(i,1);
						if( jobs.length == 0 && entry.urls.length == 0 ) {
							logMsg("Remove folder " + nodekey + " from the cart")
							delete cartData[nodekey];
						}
						return;
					}
				}
				logged_alert("Job " + nodekey + "." + jobid+ " not found in from the cart", "input Error");
			}			
		}
		this.addUrl = function(nodekey, url) {
			var entry;
//			var ch  = url.split("/");
//			var name = ch[ch.length - 1].replace(/[^\w]/, "_");
			var name = "preserve";
			if( (entry = cartData[nodekey]) == undefined ) {
				cartData[nodekey] = {jobs: new Array(), urls: new Array};
				cartData[nodekey].urls[0] = {name: name, uri: url};
			}
			else {
				var urls = entry.urls;
				for( var i=0 ; i<urls.length ; i++ ) {
					if( urls[i].uri == url ) {
						logged_alert("This url of node " + nodekey  + " is already in the cart", "input Error");
						return;
					}
				}
				cartData[nodekey].urls[i] = {name: name, uri: url};			
			}			
		}
		this.removeUrl = function(nodekey, url) {
			var entry;
			if( (entry = cartData[nodekey]) == undefined ) {
				logged_alert("There is no data associated with node " + nodekey + " in the cart", "input Error")
			}
			else {
				var urls = entry.urls;
				for( var i=0 ; i<urls.length ; i++ ) {
					if( urls[i].uri == url ) {
						logMsg("remove url from the cart");
						urls.splice(i,1);
						if( urls.length == 0 && entry.jobs.length == 0 ) {
							logMsg("Remove folder " + nodekey + " from the cart")
							delete cartData[nodekey];
						}		logger.debug("download " + zer.getUri() + " in " + fcopyName);

						return;
					}
				}
				logged_alert("URL not found in from the cart", "input Error");
			}						
		}
		this.cleanCart = function(tokenArray) {
			var old_cartData = cartData;
			cartData = {};
			for( var t=0 ; t<tokenArray.length ; t++ ) {
				var tokens = tokenArray[t];

				var tkList = tokens.split("&");
				for( var i=0 ; i<tkList.length ; i++ ){
					var row  = tkList[i].split('=');
					var num  = row[1];
					var key  = row[0].split('+');
					var node = key[0];
					if( key[1] == 'job' ) {
						that.addJobResult(node, (old_cartData[node]).jobs[num].uri);
					}
					else if( key[1] == 'url' ) {
						that.addUrl(node, (old_cartData[node]).urls[num].uri);
					}
				}
			}
			that.notifyCartCleaned();
		}

		this.changeName= function(nodekey, dataType, rowNum, newName) {
			if( dataType.toLowerCase() == "job" ) {
				cartData[nodekey].jobs[rowNum].name = newName;
			}
			else {
				cartData[nodekey].urls[rowNum].name = newName;
			}
			that.notifyCartCleaned();			
		}
		
		this.notifyCartCleaned = function() {
			$.each(listeners, function(i){
				listeners[i].isCartCleaned(cartData);
			});			
		}
		this.notifyCartOpen = function() {
			$.each(listeners, function(i){
				listeners[i].isInit(cartData);
			});			
		}

		this.startArchiveBuilding = function() {
			$.ajax({
				type: 'POST',
				url: "datapack/zipper",
				data: {PHASE: 'RUN', FORMAT: 'json',CART: JSON.stringify(cartData) },
				success: function(xmljob, status) {
					zipJob = new $.ZipjobModel(xmljob);
					setTimeout("cartView.fireCheckArchiveCompleted();", 1000);
				},
				dataType: "xml",
				error: function(xmljob, textStatus, errorThrown) {
					alert("Error: " + textStatus);
				}
			});
		}

		this.killArchiveBuilding = function() {
			if( zipJob == null ) {
				return "nojob";
			}
			else {
				zipJob.kill();
				return zipJob.phase;
			}
		}
		
		this.getJobPhase= function() {
			if( zipJob == null ) {
				return "nojob";
			}
			else {
				zipJob.refresh();
				return zipJob.phase;
			}
		}

		this.archiveDownload = function() {
			if( zipJob == null ) {
				logged_alert("There is no active ZIP builder");
			}
			else {
				zipJob.download();
			}
		}
	}
});