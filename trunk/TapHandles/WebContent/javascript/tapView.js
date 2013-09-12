jQuery.extend({

	TapView: function(){
		/**
		 * keep a reference to ourselves
		 */
		var that = this;
		/**
		 * who is listening to us?
		 */
		var listeners = new Array();
		/**
		 * add a listener to this view
		 */
		this.addListener = function(list){
			listeners.push(list);
		};
		/*
		 * Fire external events
		 */
		this.fireTreeNodeEvent = function(treepath, andsubmit){
			tapColumnSelector.fireSetTreepath(treepath, ((andsubmit)? this.fireSubmitQueryEvent: null));
			adqlQueryView.fireSetTreePath(treepath);
			adqlQueryView.fireAddConstraint("tap", "limit", [getQLimit()]);
			tapConstraintEditor.fireSetTreepath(treepath);
		};

		this.fireSubmitQueryEvent = function(){
			$.each(listeners, function(i){
				listeners[i].controlSubmitQueryEvent();
			});
		};
		this.fireRefreshJobList = function(){
			$('#tapjobs').html('');
			$.each(listeners, function(i){
				listeners[i].controlRefreshJobList();
			});
		};
		this.fireJobAction = function(nodekey, jid, session){
			$.each(listeners, function(i){
				listeners[i].controlJobAction(nodekey, jid, session);
			});
		};
		this.fireDownloadVotable = function(nodekey, jid){
			$.each(listeners, function(i){
				listeners[i].controlDownloadVotable(nodekey, jid);
			});
		};
		this.fireCheckJobCompleted= function(nodekey, jid, counter){
			$.each(listeners, function(i){
				listeners[i].controlCheckJobCompleted(nodekey, jid, counter);
			});
		};
		
		this.fireUpdateRunningJobList= function() {			
			$.each(listeners, function(i){
				listeners[i].controlUpdateRunningJobList();
			});
		};
		this.fireRemoveJob = function(id) {
			$.each(listeners, function(i){
				listeners[i].controlRemoveJob(id);
			});		
		};
		this.fireSampBroadcast= function(nodekey, jid){
			$.each(listeners, function(i){
				listeners[i].controlSampBroadcast(nodekey, jid);
			});
		};
		this.fireDisplayResult= function(nodekey, jid){
			$.each(listeners, function(i){
				listeners[i].controlDisplayResult(nodekey, jid);
			});
		};
		this.fireFilterColumns = function(val) {
			$('.kw_list').find('span').each(
					function(){

						var attr = ($(this).text().split("("))[0];
						if( val == '' || attr.indexOf(val) != -1 ) {
							$(this).parent().show();
						}
						else {
							$(this).parent().hide();							
						}
					});
		};
		this.fireChangeTable = function(newtable) {
			$.each(listeners, function(i){
				listeners[i].controlChangeTable(newtable);
			});			
		};
		/*
		 * Local processing
		 */

		this.showProgressStatus = function(){
			Modalinfo.info("Job in progress", 'Info');
		};
		this.showFailure = function(textStatus){
			Modalinfo.info("view: " + textStatus, 'Info');
		}	;	
		this.initForm= function(attributesHandlers, selectAttributesHandlers){
			/*
			 * Reset form
			 */
			//$('#adqltext').val('');
			$('#tapconstraintlist').html('');
			$('#kwalpha').html('');
			$('#kwdelta').html('');
			$('#attlist').html('');
			$('#tapselectlist').html('');
			$('#taporderby').html('');
			$('#tapselectmeta').html('');
			$('.kw_filter').val('');
			that.setNewTable(attributesHandlers, selectAttributesHandlers);
			$("#taptab").tabs({
				selected: 2
			});
		};

		this.setNewTable= function(attributesHandlers, selectAttributesHandlers){
			/*
			 * Get table columns for where clause
			 */
			var table  = "<ul class=attlist>";
			for( i in attributesHandlers  ) {
				var ah = attributesHandlers[i];
				var title = ah.description
				+ " - Name: " + ah.name
				+ " - Unit: " + ah.unit
				+ " - UCD: " + ah.ucd
				+ " - UType: " + ah.utype
				+ " - DataType: " + ah.dataType;
				
				table += "<li class=\"ui-state-default\"><span class=item title='" + title + "'>" 
					+ ah.name
					+ " (" + ah.dataType 
					+ ") " + ah.unit 
					+ "</span></li>";
			}
			table += "</select>";
			$("#tapmeta").html(table);
			$('#tapmeta span').tooltip( { 
				track: true, 
				delay: 0, 
				showURL: false, 
				opacity: 1, 
				fixPNG: true, 
				showBody: " - ", 
				// extraClass: "pretty fancy", 
				top: -15, 
				left: 5 	
			});	

			$(function() {
//				$("#tapmeta" ).sortable({
//					revert: "true"
//				});
				$( "div#tapmeta li" ).draggable({
					connectToSortable: "#tapconstraintslist",
					helper: "clone",
					revert: "invalid"
				});

			});

			/*
			 * Get table columns for select clause
			 */
			var table  = "<ul class=attlist>";
			for( i in selectAttributesHandlers  ) {
				table += "<li class=\"ui-state-default\"><span class=item>" 
					+ selectAttributesHandlers[i].name
					+ " (" + selectAttributesHandlers[i].dataType 
					+ ") " + selectAttributesHandlers[i].unit 
					+ "</span></li>";
			}
			table += "</select>";
			$("#tapselectmeta").html(table);
			$(function() {
//				$("#tapselectmeta" ).sortable({
//					revert: "true"
//				});
				$( "div#tapselectmeta li" ).draggable({
					connectToSortable: "#tapselectlist",
					helper: "clone",
					revert: "invalid"
				});

			});
			
		};
		this.coordDone= function(key, constr){
			$('#CoordList').append("<div id=" + key + "></div>");

			$('#' + key).html('<span id=' + key + '_name>' + constr + '</span>');
			$('#' + key).append('<a id=' + key + '_close href="javascript:void(0);" class=closekw></a>');
			$('#' +  key + "_close").click(function() {
				$('#' +  key).remove();
				that.fireUpdateQueryEvent();
			});
		};

		this.queryUpdated= function(query){
			$('#adqltext').val(query);
		};

		this.jobView= function(jobcontroler){
			jobcontroler.fireInitForm('tapjobs');
		};

		this.fireDisplayHisto = function(){
			var result = '';
			result += '<img src="images/histoleft-grey.png">';
			result += '<img src="images/historight-grey.png">';	
			$('#histoarrows').html('');
			$('#histoarrows').html(result);
		};
	}
});