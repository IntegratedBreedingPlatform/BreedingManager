/*global Modernizr*/
'use strict';

(function() {
	// Record whether media queries are supported in this browser as a class
	if (!Modernizr.mq('only all')) {
		$('html').addClass('no-mq');
	}
}($));
