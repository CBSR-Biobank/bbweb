/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */

/**
 * Displays a specimen link type in a modal.
 */
/* @ngInject */
function SpcLinkTypeViewerFactory(EntityViewer) {

  /**
   * FIXME: displaying of container types need to be fixed when implemented.
   */
  function SpcLinkTypeViewer(spcLinkType, processingType) {
    var ev = new EntityViewer(spcLinkType, 'Specimen Link Type');

    ev.addAttribute('Processing Type',
                    processingType.name);
    ev.addAttribute('Input Group',
                    spcLinkType.inputGroup.name);
    ev.addAttribute('Expected input change',
                    spcLinkType.expectedInputChange + ' ' + spcLinkType.inputGroup.units);
    ev.addAttribute('Input count',
                    spcLinkType.inputCount);
    ev.addAttribute('Input Container Type', 'None');
    ev.addAttribute('Output Group',
                    spcLinkType.outputGroup.name);
    ev.addAttribute('Expected output change',
                    spcLinkType.expectedInputChange + ' ' + spcLinkType.outputGroup.units);
    ev.addAttribute('Output count',
                    spcLinkType.outputCount);
    ev.addAttribute('Output Container Type', 'None');
    ev.addAttribute('Annotation Types',
                    spcLinkType.getAnnotationTypeDataAsString());

    ev.showModal();
  }

  return SpcLinkTypeViewer;
}

export default ngModule => ngModule.factory('SpcLinkTypeViewer', SpcLinkTypeViewerFactory)
