/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */

/**
 * @typedef domain.centres.CentreLocationInfo
 *
 * @type object
 *
 * @property {string} centreId - the ID that identifies the centre.
 *
 * @property {string} locationId - the ID that identifies the location.
 *
 * @property {string} name - the centre's name concatenated with the location name.
 */

const centreLocationInfoSchema = {
  'id': 'CentreLocationInfo',
  'type': 'object',
  'properties': {
    'centreId':   { 'type': 'string' },
    'locationId': { 'type': 'string' },
    'name':       { 'type': 'string' }
  },
  'required': [
    'centreId',
    'locationId',
    'name',
  ]
};

export default ngModule => ngModule.constant('centreLocationInfoSchema', centreLocationInfoSchema)
