define(['./module'], function(module) {
  'use strict';

  module.factory('AnnotationTypeViewer', AnnotationTypeViewerFactory);

  AnnotationTypeViewerFactory.$inject = ['EntityViewer'];

  /**
   *
   */
  function AnnotationTypeViewerFactory(EntityViewer) {

    function AnnotationTypeViewer(annotationType, title) {
      var ev = new EntityViewer(annotationType, title);

      ev.addAttribute('Name:', annotationType.name);
      ev.addAttribute('Type:', annotationType.valueType);

      if (typeof annotationType.required !== 'undefined') {
        ev.addAttribute('Required:', annotationType.required ? 'Yes' : 'No');
      }

      if (annotationType.valueType === 'Select') {
        if (!annotationType.options || annotationType.options.length < 1) {
          throw new Error('invalid annotation type options');
        }

        ev.addAttribute('# Selections Allowed:',
                        annotationType.maxValueCount === 1 ? 'Single' : 'Multiple');
        ev.addAttribute('Selections:', annotationType.options.join(', '));
      }

      ev.addAttribute('Description:', annotationType.description);
      ev.showModal();
    }

    return AnnotationTypeViewer;
  }

});
