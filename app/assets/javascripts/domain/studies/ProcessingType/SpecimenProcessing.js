/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/* @ngInject */
function SpecimenProcessingFactory($log,
                                   biobankApi,
                                   InputSpecimenProcessing,
                                   OutputSpecimenProcessing,
                                   DomainEntity,
                                   DomainError) {

  /*
   * Used for validating plain objects.
   */
  const SCHEMA = {
    'id': 'SpecimenProcessing',
    'type': 'object',
    'properties': {
      'input':  { '$ref': 'InputSpecimenProcessing' },
      'output': { '$ref': 'OutputSpecimenProcessing' }
    },
    'required': [ 'input', 'output' ]
  };

  /**
   * @classdesc Defines the input and output {@link domain.participants.Specimen Specimen} information for a
   * {@link domain.studies.ProcessingType ProcessingType}.
   *
   * @class
   * @memberOf domain.studies
   */
  class SpecimenProcessing extends DomainEntity {

    constructor(obj = {}) {
      /**
       * The information for the input specimen.
       *
       * @name domain.studies.SpecimenProcessing#input
       * @type {domain.studies.InputSpecimenProcessing}
       */

      /**
       * The information for the output specimen.
       *
       * @name domain.studies.SpecimenProcessing#output
       * @type {domain.studies.OutputSpecimenProcessing}
       */

      super(Object.assign(
        {
          input: undefined,
          output: undefined
        },
        obj
      ));

      this.input  = new InputSpecimenProcessing(obj.input);
      this.output = new OutputSpecimenProcessing(obj.output);
    }

    /**
     * @private
     * @return {object} The JSON schema for this class.
     */
    static schema() {
      return SCHEMA;
    }

    /**
     * @private
     */
    static additionalSchemas() {
      return [ InputSpecimenProcessing.schema(), OutputSpecimenProcessing.schema() ]
        .concat(InputSpecimenProcessing.additionalSchemas())
        .concat(OutputSpecimenProcessing.additionalSchemas());
    }

    /**
     * Creates a SpecimenProcessing object, but first it validates `obj` to ensure that it has a valid schema.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {domain.studies.InputSpecimenProcessing} An object containing the specimen processing
     * information.
     */
    static create(obj) {
      var validation = SpecimenProcessing.isValid(obj);
      if (!validation.valid) {
        $log.error(validation.message);
        throw new DomainError(validation.message);
      }
      return new SpecimenProcessing(obj);
    }

  }

  return SpecimenProcessing;
}

export default ngModule => ngModule.factory('SpecimenProcessing', SpecimenProcessingFactory)
