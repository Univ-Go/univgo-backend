-- A space's description and its rules are per space, not per category: two sports courts share a
-- taxonomy and not a set of instructions, and the rules a student must read before booking the gym
-- have nothing to do with the ones for a tennis court. They were UI copy keyed by category on the
-- frontend, which could never say anything true about one room.
--
-- One column per concept and one language. Who writes them is an administrator through a CRUD that
-- does not exist yet; when it does, the language is their choice, not the build's.
ALTER TABLE spaces ADD COLUMN description TEXT;

-- An empty array is a space whose rules nobody has written yet, which is a real state and not a
-- fault: the views drop the section. Ordering is the array's own, which is the order they are read
-- in, and the future CRUD rewrites the list whole.
ALTER TABLE spaces ADD COLUMN rules TEXT[] NOT NULL DEFAULT '{}';

-- Placeholder copy, like spaces.location in V11: the real text has to be asked of the university.
-- Matched by name rather than by id so this works on a database whose spaces were seeded fresh.
UPDATE spaces SET
  description = 'Cancha de fútbol 11 en grama sintética, con arcos reglamentarios e iluminación para la jornada de la tarde. Pensada para partidos completos, entrenamientos de equipo y clases de la facultad de deportes.',
  rules = ARRAY[
    'Usa guayos de grama sintética. No se permiten tacos de aluminio.',
    'La cancha se reserva completa: coordina con tu equipo antes de reservar.',
    'Deja la cancha despejada de botellas, conos y petos al terminar.',
    'Cualquier daño en la malla o en los arcos debe reportarse al encargado.'
  ]
WHERE name = 'Cancha de Grama sintética (11)';

UPDATE spaces SET
  description = 'Cancha de fútbol 7 en grama sintética, más corta que la de once y con arcos de medida reducida. Ideal para partidos rápidos, entrenamientos por grupos pequeños y torneos internos.',
  rules = ARRAY[
    'Usa guayos de grama sintética. No se permiten tacos de aluminio.',
    'La cancha se reserva completa: coordina con tu equipo antes de reservar.',
    'Deja la cancha despejada de botellas, conos y petos al terminar.'
  ]
WHERE name = 'Cancha de Grama sintética (7)';

UPDATE spaces SET
  description = 'Cancha de tenis de campo en superficie dura, con red reglamentaria y zona de banca a la sombra. Pensada para partidos individuales o de dobles y para prácticas libres.',
  rules = ARRAY[
    'Usa calzado de tenis de suela lisa: otras suelas marcan la superficie.',
    'Trae tus propias raquetas y pelotas; el espacio no las presta.',
    'Recoge las pelotas de toda la cancha antes de salir.'
  ]
WHERE name = 'Cancha de Tenis de Campo A';

UPDATE spaces SET
  description = 'Cancha de tenis de campo en superficie dura, contigua a la cancha A y con las mismas medidas. Pensada para partidos individuales o de dobles y para prácticas libres.',
  rules = ARRAY[
    'Usa calzado de tenis de suela lisa: otras suelas marcan la superficie.',
    'Trae tus propias raquetas y pelotas; el espacio no las presta.',
    'Recoge las pelotas de toda la cancha antes de salir.'
  ]
WHERE name = 'Cancha de Tenis de Campo B';

UPDATE spaces SET
  description = 'Cancha de básquetbol techada, con tableros regulables y marcador manual. Sirve para partidos cinco contra cinco, entrenamientos de tiro y clases dirigidas.',
  rules = ARRAY[
    'Usa calzado deportivo de suela limpia dentro de la cancha.',
    'Guarda los balones en el carro al terminar la reserva.',
    'No cuelgues del aro ni muevas los tableros sin el encargado.'
  ]
WHERE name = 'Cancha de básquetbol';

UPDATE spaces SET
  description = 'Sala de musculación y cardio con máquinas de fuerza, peso libre y bicicletas estáticas. Abierta al uso individual durante todo el bloque reservado, con un monitor disponible para orientar la rutina.',
  rules = ARRAY[
    'Usa toalla sobre las máquinas y límpialas después de cada ejercicio.',
    'Devuelve las mancuernas y los discos a su soporte.',
    'No se permite entrenar con el torso descubierto ni con sandalias.',
    'Avisa al monitor ante cualquier molestia o fallo de una máquina.'
  ]
WHERE name = 'Gimnasio';

-- Backfilled above, so the column can carry the invariant the product wants: a listed space always
-- has something to say about itself. Same reasoning as users.email in V10 and spaces.location in V11.
ALTER TABLE spaces ALTER COLUMN description SET NOT NULL;
