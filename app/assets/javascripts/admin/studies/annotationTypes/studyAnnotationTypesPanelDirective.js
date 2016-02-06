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
      scope: {
        study:                  '=',
        annotationTypes:        '=',
        annotationTypeIdsInUse: '=',
        annotationTypeName:     '@',
        panelId:                '@',
        addStateName:           '@',
        updateStateName:        '@'
      },
      templateUrl: '/assets/javascripts/admin/studies/annotationTypes/studyAnnotationTypesPanel.html',
      controller: StudyAnnotationTypesPanelCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  StudyAnnotationTypesPanelCtrl.$inject = [ '$scope', 'Panel' ];

  /**
   * A panel to display a study's participant annotation types.
   */
  function StudyAnnotationTypesPanelCtrl($scope,
                                    Panel) {
    var vm = this,
        panel = new Panel($scope.panelId, $scope.addStateName);

    vm.study                     = $scope.study;
    vm.annotationTypes           = $scope.annotationTypes;
    vm.annotationTypeIdsInUse    = $scope.annotationTypeIdsInUse;
    vm.annotationTypeName        = $scope.annotationTypeName;
    vm.panelHeading              = panelHeading($scope.annotationTypeName);
    vm.annotationTypeDescription = annotationTypeDescription($scope.annotationTypeName);
    vm.updateStateName           = $scope.updateStateName;
    vm.add                       = add;
    vm.panelOpen                 = panel.getPanelOpenState();
    vm.modificationsAllowed      = vm.study.isDisabled();

    $scope.$watch(angular.bind(vm, function() { return vm.panelOpen; }),
                  angular.bind(panel, panel.watchPanelOpenChangeFunc));

    //--

    function add() {
      return panel.add();
    }

    function panelHeading(annotationTypeName) {
      switch (annotationTypeName) {
      case 'ParticipantAnnotationType':
        return 'Participant Annotation Types';

      case 'CollectionEventAnnotationType':
        return 'Collection Event Annotation Types';

      case 'SpecimenLinkAnnotationType':
        return 'Specimen Link Annotation Types';

      default:
        throw new Error('invalid annotation type name: ' + vm.annotationTypeName);
      }
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
