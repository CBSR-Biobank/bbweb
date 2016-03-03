/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function (_) {
  'use strict';

  /**
   *
   */
  function collectionEventAnnotationTypeViewDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        collectionEventType: '=',
        annotationType:      '='
      },
      template: [
        '<annotation-type-view',
        '  study="vm.study"',
        '  annotation-type="vm.annotationType"',
        '  return-state="home.admin.studies.study.collection.view"',
        '  on-update="vm.onUpdate">',
        '</annotation-type-view>'
      ].join(''),
      controller: CollectionEventAnnotationTypeViewCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  CollectionEventAnnotationTypeViewCtrl.$inject = [
    'notificationsService'
  ];

  function CollectionEventAnnotationTypeViewCtrl(notificationsService) {
    var vm = this;

    vm.onUpdate = onUpdate;

    function onUpdate(annotationType) {
      vm.collectionEventType.updateAnnotationType(annotationType)
        .then(postUpdate('Annotation type changed successfully.',
                         'Change successful',
                         1500))
        .catch(notificationsService.updateError);
    }

    function postUpdate(message, title, timeout) {
      return function (collectionEventType) {
        vm.collectionEventType = collectionEventType;
        vm.annotationType = _.findWhere(vm.collectionEventType.annotationTypes,
                                        { uniqueId: vm.annotationType.uniqueId });
        if (_.isUndefined(vm.annotationType)) {
          throw new Error('could not update annotation type');
        }
        notificationsService.success(message, title, timeout);
      };
    }

  }

  return collectionEventAnnotationTypeViewDirective;

});
