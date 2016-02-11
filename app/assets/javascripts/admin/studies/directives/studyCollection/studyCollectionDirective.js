/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([], function() {
  'use strict';

  /**
   * Description
   */
  function studyCollectionDirective() {
    var directive = {
      restrict: 'E',
      scope: {
        study: '=',
        collectionDto: '='
      },
      templateUrl : '/assets/javascripts/admin/studies/directives/studyCollection/studyCollection.html'
    };
    return directive;
  }

  return studyCollectionDirective;

});
