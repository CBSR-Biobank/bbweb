define([], function() {
  'use strict';

  annotationTypeViewerFactory.$inject = [
    'EntityViewer',
    'ParticipantAnnotationType'
  ];

  /**
   *
   */
  function annotationTypeViewerFactory(EntityViewer,
                                      ParticipantAnnotationType) {

    function AnnotationTypeViewer(annotationType, title) {
      var ev = new EntityViewer(annotationType, title);

      ev.addAttribute('Name', annotationType.name);
      ev.addAttribute('Type', annotationType.valueType);

      if (annotationType instanceof ParticipantAnnotationType) {
        ev.addAttribute('Required', annotationType.required ? 'Yes' : 'No');
      }

      if (annotationType.isValueTypeSelect()) {
        if (!annotationType.options || annotationType.options.length < 1) {
          throw new Error('invalid annotation type options');
        }

        ev.addAttribute('Selections Allowed',
                        annotationType.isSingleSelect() ? 'Single' : 'Multiple');
        ev.addAttribute('Selections', annotationType.options.join(', '));
      }

      ev.addAttribute('Description', annotationType.description);
      ev.showModal();
    }

    return AnnotationTypeViewer;
  }

  return annotationTypeViewerFactory;
});
