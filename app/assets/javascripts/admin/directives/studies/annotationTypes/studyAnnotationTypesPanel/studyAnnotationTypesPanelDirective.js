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
      require: '^tab',
      restrict: 'E',
      scope: {},
      bindToController: {
        study:                  '=',
        annotationTypes:        '=',
        annotationTypeIdsInUse: '=',
        annotationTypeName:     '@',
        panelId:                '@',
        addStateName:           '@',
        viewStateName:          '@',
        onRemove:               '&'
      },
      templateUrl: '/assets/javascripts/admin/directives/studies/annotationTypes/studyAnnotationTypesPanel/studyAnnotationTypesPanel.html',
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

    vm.annotationTypeDescription = annotationTypeDescription(vm.annotationTypeName);
    vm.add                       = add;
    vm.panelOpen                 = panel.getPanelOpenState();
    vm.modificationsAllowed      = vm.study.isDisabled();
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

    function annotationTypeDescription(annotationTypeName) {
      switch (annotationTypeName) {
      case 'ParticipantAnnotationType':
        return 'Participant annotations allow a study to collect custom named and ' +
          'defined pieces of data for each participant. Annotations are optional and ' +
          'are not required to be defined.';

      case 'CollectionEventAnnotationType':
        return 'Collection event annotations allow a study to collect custom named and ' +
          'defined pieces of data for each collection event. Annotations are optional and ' +
          'are not required to be defined.';

      case 'SpecimenLinkAnnotationType':
        return 'Specimen link annotations allow a study to collect custom named and '+
          'defined pieces of data when processing specimens. Annotations are optional and ' +
          'are not required to be defined.';

      default:
        throw new Error('invalid annotation type name: ' + vm.annotationTypeName);
      }

    }
  }

  return studyAnnotationTypesPanelDirective;
});
