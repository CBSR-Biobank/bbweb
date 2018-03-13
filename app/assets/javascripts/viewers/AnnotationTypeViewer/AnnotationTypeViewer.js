/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 *
 */
/* @ngInject */
function AnnotationTypeViewerFactory(EntityViewer) {

  function AnnotationTypeViewer(annotationType, title) {
    var ev = new EntityViewer(annotationType, title);

    ev.addAttribute('Name', annotationType.name);
    ev.addAttribute('Required', annotationType.required ? 'Yes' : 'No');

    ev.addAttribute('Type', annotationType.getValueTypeLabelFunc());
    if (annotationType.isValueTypeSelect()) {
      if (!annotationType.options || annotationType.options.length < 1) {
        throw new Error('invalid annotation type options');
      }

      ev.addAttribute('Selections', annotationType.options.join(', '));
    }

    ev.addAttribute('Description', annotationType.description);
    ev.showModal();
  }

  return AnnotationTypeViewer;
}

export default ngModule => ngModule.factory('AnnotationTypeViewer', AnnotationTypeViewerFactory)
