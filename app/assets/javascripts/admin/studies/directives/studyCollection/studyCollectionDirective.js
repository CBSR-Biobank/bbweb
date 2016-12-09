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
        study: '='
      },
      templateUrl : '/assets/javascripts/admin/studies/directives/studyCollection/studyCollection.html',
      controller: StudyCollectionCtrl,
      controllerAs: 'vm'
    };
    return directive;
  }

  StudyCollectionCtrl.$inject = ['$scope'];

  function StudyCollectionCtrl($scope) {
    init();

    //--

    function init() {
      // updates the selected tab in 'studyViewDirective' which is the parent directive
      $scope.$emit('tabbed-page-update', 'collections-tab-selected');
    }

  }

  return studyCollectionDirective;
});
