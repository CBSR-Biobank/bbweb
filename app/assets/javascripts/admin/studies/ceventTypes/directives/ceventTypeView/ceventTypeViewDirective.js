/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   *
   */
  function ceventTypeViewDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        ceventType: '='
      },
      templateUrl : '/assets/javascripts/admin/studies/ceventTypes/directives/ceventTypeView/ceventTypeView.html',
      controller: CeventTypeViewCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  CeventTypeViewCtrl.$inject = [ '$state' ];

  function CeventTypeViewCtrl($state) {
    var vm = this;

    vm.addAnnotationType = addAnnotationType;
    vm.addSpecimenGroup = addSpecimenGroup;

    //--

    function addAnnotationType() {
      $state.go('home.admin.studies.study.collection.view.annotationTypeAdd');
    }

    function addSpecimenGroup() {
      $state.go('home.admin.studies.study.collection.view.specimenGroupAdd');
    }
  }

  return ceventTypeViewDirective;

});
