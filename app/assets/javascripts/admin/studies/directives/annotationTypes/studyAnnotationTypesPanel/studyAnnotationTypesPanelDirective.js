/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular', 'underscore'], function(angular, _) {
  'use strict';

  /**
   *
   */
  function studyAnnotationTypesPanelDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        annotationTypes:           '=',
        annotationTypeIdsInUse:    '=',
        annotationTypeDescription: '@',
        annotationTypeName:        '@',
        panelId:                   '@',
        modificationsAllowed:      '=',
        addStateName:              '@',
        viewStateName:             '@',
        onRemove:                  '&'
      },
      templateUrl: '/assets/javascripts/admin/studies/directives/annotationTypes/studyAnnotationTypesPanel/studyAnnotationTypesPanel.html',
      controller: StudyAnnotationTypesPanelCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  StudyAnnotationTypesPanelCtrl.$inject = [ '$scope', '$state', 'Panel' ];

  /**
   * A panel to display a study's participant annotation types.
   */
  function StudyAnnotationTypesPanelCtrl($scope, $state, Panel) {
    var vm = this,
        panel = new Panel(vm.panelId, vm.addStateName);

    vm.add                       = add;
    vm.panelOpen                 = panel.getPanelOpenState();
    vm.onAnnotTypeRemove         = onAnnotTypeRemove;

    $scope.$watch(angular.bind(vm, function() { return vm.panelOpen; }),
                  angular.bind(panel, panel.watchPanelOpenChangeFunc));

    //--

    function add() {
      $state.go(vm.addStateName);
    }

    // remove returns a promise
    function onAnnotTypeRemove(annotType) {
      return vm.onRemove()(annotType);
    }
  }

  return studyAnnotationTypesPanelDirective;
});
